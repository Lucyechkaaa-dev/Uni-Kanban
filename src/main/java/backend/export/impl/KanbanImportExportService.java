package backend.export.impl;

import backend.models.task.Board;
import backend.models.team.Team;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public interface KanbanImportExportService {

    String exportToJson(Board table);

    void exportToJson(Board table, OutputStream outputStream);

    void exportToJson(Board table, File file);

    Board importFromJson(String jsonContent, Team targetTeam);

    Board importFromJson(InputStream inputStream, Team targetTeam);

    Board importFromJson(File file, Team targetTeam);

    String exportToCsv(Board table);

    void exportToCsv(Board table, OutputStream outputStream);

    void exportToCsv(Board table, File file);

    Board importFromCsv(String csvContent, Team targetTeam);

    Board importFromCsv(InputStream inputStream, Team targetTeam);

    Board importFromCsv(File file, Team targetTeam);
}
