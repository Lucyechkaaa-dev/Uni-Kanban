package backend.export.dto;

import backend.models.task.Task;
import backend.models.task.TaskList;
import backend.models.task.Board;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardExportDto {
    private String name;
    private List<ColumnExportDto> columns = new ArrayList<>();

    public BoardExportDto(String name) {
        this.name = name;
        this.columns = new ArrayList<>();
    }

    public static BoardExportDto fromEntity(Board table) {
        if (table == null) {
            return new BoardExportDto();
        }
        BoardExportDto boardDto = new BoardExportDto(table.getName());
        if (table.getTaskLists() != null) {
            for (TaskList list : table.getTaskLists()) {
                ColumnExportDto colDto = new ColumnExportDto(list.getName());
                if (list.getTasks() != null) {
                    for (Task task : list.getTasks()) {
                        String assignee = (task.getUserOwner() != null) ? task.getUserOwner().getUsername() : null;
                        colDto.getTasks().add(new TaskExportDto(
                                task.getTitle(),
                                task.getDescription(),
                                task.getPosition(),
                                assignee
                        ));
                    }
                }
                boardDto.getColumns().add(colDto);
            }
        }
        return boardDto;
    }
}
