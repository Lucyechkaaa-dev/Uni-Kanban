package backend.services.export;

import backend.models.task.TaskTable;
import backend.models.team.Team;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public interface KanbanImportExportService {

    String exportToJson(TaskTable table);

    void exportToJson(TaskTable table, OutputStream outputStream);

    void exportToJson(TaskTable table, File file);

    TaskTable importFromJson(String jsonContent, Team targetTeam);

    TaskTable importFromJson(InputStream inputStream, Team targetTeam);

    TaskTable importFromJson(File file, Team targetTeam);

    String exportToCsv(TaskTable table);

    void exportToCsv(TaskTable table, OutputStream outputStream);

    void exportToCsv(TaskTable table, File file);

    TaskTable importFromCsv(String csvContent, Team targetTeam);

    TaskTable importFromCsv(InputStream inputStream, Team targetTeam);

    TaskTable importFromCsv(File file, Team targetTeam);
}
