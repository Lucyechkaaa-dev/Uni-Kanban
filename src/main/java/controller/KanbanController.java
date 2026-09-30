package controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kanban")
@CrossOrigin(origins = "*")
public class KanbanController {

    @GetMapping("/status")
    public String status() {
        return "andrej shkipper terpila";
    }
}