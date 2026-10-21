package pe.unp.abastecimiento.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PortalController {

    @GetMapping("/")
    public String portal() {
        return "index";
    }

    @GetMapping({"/api", "/api/"})
    public String api() {
        return "api";
    }
}
