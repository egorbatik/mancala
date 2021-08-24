package com.egorbatik.mancala.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
public class Board {
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
	@Getter
	@Setter
    private Long id;
	
	@Getter
	@Setter
	private PlayerType nextPlayer;
	
	
	@Column
	@Getter
	@Setter
	private Integer[] topPlayerBoard;
	
	@Column
	@Getter
	@Setter
	private Integer[] bottomPlayerBoard;

	
}
