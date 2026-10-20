package backend.services.export;

import backend.dao.UserDao;
import backend.models.task.Task;
import backend.models.task.TaskList;
import backend.models.task.TaskTable;
import backend.models.team.Team;
import backend.models.team.UserTeam;
import backend.models.user.User;
import backend.services.export.dto.BoardExportDto;
import backend.services.export.dto.ColumnExportDto;
import backend.services.export.dto.TaskExportDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Log4j2
@Service
public class KanbanImportExportServiceImpl implements KanbanImportExportService {

    private static final String UTF8_BOM = "\uFEFF";
    private static final char DEFAULT_CSV_DELIMITER = ';';
    private static final String[] CSV_HEADERS = {"Board", "Column", "Task Title", "Description", "Assignee", "Position"};

    private final ObjectMapper objectMapper;
    private final UserDao userDao;

    @Autowired
    public KanbanImportExportServiceImpl(UserDao userDao) {
        this.userDao = userDao;
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public KanbanImportExportServiceImpl() {
        this(new UserDao());
    }

    @Override
    public String exportToJson(TaskTable table) {
        Objects.requireNonNull(table, "TaskTable cannot be null for export");
        log.info("Exporting board '{}' to JSON", table.getName());
        try {
            BoardExportDto dto = BoardExportDto.fromEntity(table);
            return objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            log.error("Failed to export board to JSON", e);
            throw new RuntimeException("Failed to export board to JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public void exportToJson(TaskTable table, OutputStream outputStream) {
        Objects.requireNonNull(outputStream, "OutputStream cannot be null");
        String json = exportToJson(table);
        try {
            outputStream.write(json.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } catch (IOException e) {
            log.error("Failed writing JSON to OutputStream", e);
            throw new RuntimeException("Failed writing JSON to OutputStream: " + e.getMessage(), e);
        }
    }

    @Override
    public void exportToJson(TaskTable table, File file) {
        Objects.requireNonNull(file, "File cannot be null");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            exportToJson(table, fos);
            log.info("Successfully exported board to JSON file: {}", file.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed writing JSON to file: {}", file.getAbsolutePath(), e);
            throw new RuntimeException("Failed writing JSON to file: " + e.getMessage(), e);
        }
    }

    @Override
    public TaskTable importFromJson(String jsonContent, Team targetTeam) {
        if (jsonContent == null || jsonContent.isBlank()) {
            throw new IllegalArgumentException("JSON content cannot be empty");
        }
        log.info("Importing board from JSON");
        try {
            BoardExportDto dto = objectMapper.readValue(jsonContent, BoardExportDto.class);
            return convertDtoToEntity(dto, targetTeam);
        } catch (Exception e) {
            log.error("Failed to import board from JSON", e);
            throw new RuntimeException("Failed to parse JSON content: " + e.getMessage(), e);
        }
    }

    @Override
    public TaskTable importFromJson(InputStream inputStream, Team targetTeam) {
        Objects.requireNonNull(inputStream, "InputStream cannot be null");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return importFromJson(sb.toString(), targetTeam);
        } catch (IOException e) {
            log.error("Failed reading JSON from InputStream", e);
            throw new RuntimeException("Failed reading JSON from InputStream: " + e.getMessage(), e);
        }
    }

    @Override
    public TaskTable importFromJson(File file, Team targetTeam) {
        Objects.requireNonNull(file, "File cannot be null");
        try (FileInputStream fis = new FileInputStream(file)) {
            return importFromJson(fis, targetTeam);
        } catch (IOException e) {
            log.error("Failed importing JSON from file: {}", file.getAbsolutePath(), e);
            throw new RuntimeException("Failed importing JSON from file: " + e.getMessage(), e);
        }
    }

    @Override
    public String exportToCsv(TaskTable table) {
        Objects.requireNonNull(table, "TaskTable cannot be null for export");
        log.info("Exporting board '{}' to CSV for Excel", table.getName());

        StringWriter writer = new StringWriter();
        // Write UTF-8 BOM so Excel opens Cyrillic & UTF-8 smoothly without encoding issues
        writer.write(UTF8_BOM);

        CSVFormat format = CSVFormat.EXCEL.builder()
                .setDelimiter(DEFAULT_CSV_DELIMITER)
                .setHeader(CSV_HEADERS)
                .setRecordSeparator("\r\n")
                .build();

        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            String boardName = (table.getName() != null) ? table.getName() : "Board";

            if (table.getTaskLists() == null || table.getTaskLists().isEmpty()) {
                printer.printRecord(boardName, "", "", "", "", "");
            } else {
                for (TaskList list : table.getTaskLists()) {
                    String colName = (list.getName() != null) ? list.getName() : "Column";
                    List<Task> tasks = list.getTasks();

                    if (tasks == null || tasks.isEmpty()) {
                        // Keep empty columns in the export
                        printer.printRecord(boardName, colName, "", "", "", "");
                    } else {
                        for (Task task : tasks) {
                            String title = (task.getTitle() != null) ? task.getTitle() : "";
                            String desc = (task.getDescription() != null) ? task.getDescription() : "";
                            String assignee = (task.getUser() != null && task.getUser().getUsername() != null)
                                    ? task.getUser().getUsername()
                                    : "";
                            int pos = task.getPosition();
                            printer.printRecord(boardName, colName, title, desc, assignee, pos);
                        }
                    }
                }
            }
            printer.flush();
            return writer.toString();
        } catch (IOException e) {
            log.error("Failed exporting board to CSV", e);
            throw new RuntimeException("Failed exporting board to CSV: " + e.getMessage(), e);
        }
    }

    @Override
    public void exportToCsv(TaskTable table, OutputStream outputStream) {
        Objects.requireNonNull(outputStream, "OutputStream cannot be null");
        String csv = exportToCsv(table);
        try {
            outputStream.write(csv.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } catch (IOException e) {
            log.error("Failed writing CSV to OutputStream", e);
            throw new RuntimeException("Failed writing CSV to OutputStream: " + e.getMessage(), e);
        }
    }

    @Override
    public void exportToCsv(TaskTable table, File file) {
        Objects.requireNonNull(file, "File cannot be null");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            exportToCsv(table, fos);
            log.info("Successfully exported board to CSV file: {}", file.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed writing CSV to file: {}", file.getAbsolutePath(), e);
            throw new RuntimeException("Failed writing CSV to file: " + e.getMessage(), e);
        }
    }

    @Override
    public TaskTable importFromCsv(String csvContent, Team targetTeam) {
        if (csvContent == null || csvContent.isBlank()) {
            throw new IllegalArgumentException("CSV content cannot be empty");
        }
        log.info("Importing board from CSV");

        String sanitized = csvContent.startsWith(UTF8_BOM) ? csvContent.substring(1) : csvContent;
        char delimiter = detectDelimiter(sanitized);

        CSVFormat format = CSVFormat.EXCEL.builder()
                .setDelimiter(delimiter)
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();

        try (Reader reader = new StringReader(sanitized);
             CSVParser parser = new CSVParser(reader, format)) {

            String boardName = null;
            Map<String, TaskList> columnsMap = new LinkedHashMap<>();
            TaskTable table = new TaskTable();
            if (targetTeam != null) {
                table.setTeam(targetTeam);
            }

            for (CSVRecord record : parser) {
                if (boardName == null) {
                    boardName = getRecordValue(record, "Board", 0);
                    if (boardName != null && !boardName.isBlank()) {
                        table.setName(boardName);
                    }
                }

                String colName = getRecordValue(record, "Column", 1);
                if (colName == null || colName.isBlank()) {
                    continue;
                }

                TaskList column = columnsMap.computeIfAbsent(colName, name -> {
                    TaskList newCol = new TaskList(name);
                    table.addTaskList(newCol);
                    return newCol;
                });

                String taskTitle = getRecordValue(record, "Task Title", 2);
                if (taskTitle == null || taskTitle.isBlank()) {
                    // Try alternate header name "Title"
                    taskTitle = getRecordValue(record, "Title", 2);
                }

                if (taskTitle != null && !taskTitle.isBlank()) {
                    String desc = getRecordValue(record, "Description", 3);
                    String assigneeName = getRecordValue(record, "Assignee", 4);
                    String posStr = getRecordValue(record, "Position", 5);

                    int position = 0;
                    if (posStr != null && !posStr.isBlank()) {
                        try {
                            position = Integer.parseInt(posStr);
                        } catch (NumberFormatException ignored) {}
                    }

                    Task task = new Task(taskTitle);
                    task.setDescription(desc);
                    task.setPosition(position);

                    if (assigneeName != null && !assigneeName.isBlank()) {
                        User assignee = resolveUser(assigneeName, targetTeam);
                        task.setUser(assignee);
                    }

                    column.addTask(task);
                }
            }

            if (table.getName() == null || table.getName().isBlank()) {
                table.setName("Imported Board");
            }

            log.info("Successfully imported board '{}' with {} columns from CSV",
                    table.getName(), table.getTaskLists().size());
            return table;

        } catch (Exception e) {
            log.error("Failed to parse CSV content", e);
            throw new RuntimeException("Failed to parse CSV content: " + e.getMessage(), e);
        }
    }

    @Override
    public TaskTable importFromCsv(InputStream inputStream, Team targetTeam) {
        Objects.requireNonNull(inputStream, "InputStream cannot be null");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return importFromCsv(sb.toString(), targetTeam);
        } catch (IOException e) {
            log.error("Failed reading CSV from InputStream", e);
            throw new RuntimeException("Failed reading CSV from InputStream: " + e.getMessage(), e);
        }
    }

    @Override
    public TaskTable importFromCsv(File file, Team targetTeam) {
        Objects.requireNonNull(file, "File cannot be null");
        try (FileInputStream fis = new FileInputStream(file)) {
            return importFromCsv(fis, targetTeam);
        } catch (IOException e) {
            log.error("Failed importing CSV from file: {}", file.getAbsolutePath(), e);
            throw new RuntimeException("Failed importing CSV from file: " + e.getMessage(), e);
        }
    }

    private TaskTable convertDtoToEntity(BoardExportDto dto, Team targetTeam) {
        TaskTable table = new TaskTable(dto.getName() != null ? dto.getName() : "Imported Board");
        if (targetTeam != null) {
            table.setTeam(targetTeam);
        }

        if (dto.getColumns() != null) {
            for (ColumnExportDto colDto : dto.getColumns()) {
                TaskList column = new TaskList(colDto.getName() != null ? colDto.getName() : "Column");
                table.addTaskList(column);

                if (colDto.getTasks() != null) {
                    for (TaskExportDto taskDto : colDto.getTasks()) {
                        Task task = new Task(taskDto.getTitle() != null ? taskDto.getTitle() : "Task");
                        task.setDescription(taskDto.getDescription());
                        task.setPosition(taskDto.getPosition());

                        if (taskDto.getAssignee() != null && !taskDto.getAssignee().isBlank()) {
                            User assignee = resolveUser(taskDto.getAssignee(), targetTeam);
                            task.setUser(assignee);
                        }

                        column.addTask(task);
                    }
                }
            }
        }
        return table;
    }

    private User resolveUser(String username, Team targetTeam) {
        String clean = username.trim().startsWith("@") ? username.trim().substring(1) : username.trim();

        // 1. Check in targetTeam members
        if (targetTeam != null && targetTeam.getMembers() != null) {
            for (UserTeam ut : targetTeam.getMembers()) {
                if (ut.getUser() != null && clean.equalsIgnoreCase(ut.getUser().getUsername())) {
                    return ut.getUser();
                }
            }
        }

        // 2. Check in database via userDao
        if (userDao != null) {
            try {
                User dbUser = userDao.findByUsername(clean);
                if (dbUser != null) {
                    return dbUser;
                }
            } catch (Exception ignored) {
                // If DB is offline/in test mode, proceed to fallback
            }
        }

        // 3. Fallback: lightweight representation
        return new User(clean, "pass");
    }

    private char detectDelimiter(String csv) {
        String firstLine = csv.lines().findFirst().orElse("");
        long semicolonCount = firstLine.chars().filter(ch -> ch == ';').count();
        long commaCount = firstLine.chars().filter(ch -> ch == ',').count();
        return (semicolonCount >= commaCount && semicolonCount > 0) ? ';' : ',';
    }

    private String getRecordValue(CSVRecord record, String headerName, int fallbackIndex) {
        if (record.isMapped(headerName)) {
            return record.get(headerName);
        }
        if (fallbackIndex >= 0 && fallbackIndex < record.size()) {
            return record.get(fallbackIndex);
        }
        return null;
    }
}
