package backend.export.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskExportDto {
    private String title;
    private String description;
    private int position;
    private String assignee;
}
