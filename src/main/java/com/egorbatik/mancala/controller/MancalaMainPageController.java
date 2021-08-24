package com.egorbatik.mancala.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MancalaMainPageController {
  @GetMapping("/")
  public String index() {
    return "Greetings from Spring Boot!";
  }

}
