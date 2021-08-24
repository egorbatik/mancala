package com.egorbatik.mancala.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.egorbatik.mancala.model.Board;
import com.egorbatik.mancala.model.PlayerType;
import com.egorbatik.mancala.service.BoardService;

@Controller
public class BoardController {

  @Autowired
  BoardService boardService;

  /**
   * This service methods brings the "requested board" at "current state" tailored for "requesting player"
   * @param boardId
   * @param player
   * @param model
   * @return
   */
  @GetMapping("/board")
  public String board(@RequestParam(name = "board_id", required = false) Long boardId,
      @RequestParam(name = "player", required = false) String player, Model model) {

    final PlayerType currentPlayer = boardService.resolvePlayer(Optional.ofNullable(player));
    final Board board = boardService.getBoard(Optional.ofNullable(boardId));
    model.addAttribute("board", board);
    model.addAttribute("currentPlayer", currentPlayer);
    return "board";
  }

  
  
  /**
   * This apply the selection of a house.
   * Should be a post but I'm doing the UI with HREF, without forms or Javascript 
   * as is not the focus of this challenge
   * @param boardId
   * @param player
   * @param house
   * @param model
   * @return
   */
  @GetMapping("/apply")
  public String board(@RequestParam(name = "board_id", required = false) Long boardId,
      @RequestParam(name = "player", required = false) String player,
      @RequestParam(name = "house", required = false) Integer house, Model model) {

    boardService.applyGame(boardId, PlayerType.valueOf(player.toUpperCase()), house);
    //Go back to the board. If there is a "forged/invalid" request from the UI with throw an exception.
    return "redirect:/board?board_id=" + boardId + "&player=" + player;
  }

}
