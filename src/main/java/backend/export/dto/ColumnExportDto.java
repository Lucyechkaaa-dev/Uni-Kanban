package backend.export.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColumnExportDto {
    private String name;
    private List<TaskExportDto> tasks = new ArrayList<>();

    public ColumnExportDto(String name) {
        this.name = name;
        this.tasks = new ArrayList<>();
    }
}
