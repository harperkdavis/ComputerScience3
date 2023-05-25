// Author: Harper Davis
use std::str::FromStr;

use crate::{state::State, action::Action};
#[derive(PartialEq, Clone)]
pub struct FifteenPuzzle {
    board: u64
}

impl FifteenPuzzle {
    
    pub fn new(board: u64) -> FifteenPuzzle {
        FifteenPuzzle { board }
    }

    pub fn default() -> FifteenPuzzle {
        FifteenPuzzle { board: Self::default_board() }
    }

    fn default_board() -> u64 {
        0xfedcba9876543210
    }

    pub fn get_piece(&self, index: u8) -> u8 {
        ((self.board >> (index * 4)) % 16) as u8
    }
    
    pub fn find_piece_index(&self, piece: u8) -> u8 {
        for i in 0..16 {
            if ((self.board >> (i * 4)) % 16) as u8 == piece { return i }
        }
        return 0;
    }

    pub fn move_piece(&self, piece: u8) -> u64 {
        let piece_index = self.find_piece_index(piece);
        let open_index = self.find_piece_index(0);

        let mut new_board = self.board;

        for i in 0..4 {
            new_board &= 0xffffffffffffffff - (1 << (piece_index * 4 + i));
            new_board &= 0xffffffffffffffff - (1 << (open_index * 4 + i));
            new_board |= ((piece as u64 >> i) % 2) << (open_index * 4 + i);
        }

        new_board
    }

}

impl ToString for FifteenPuzzle {

    fn to_string(&self) -> String {
        format!("{:#018x}", self.board)
    }

}

impl State for FifteenPuzzle {
    type Action = FifteenPuzzleAction;

    fn display_pretty(&self) {
        println!("+---+---+---+---+");
        for i in 0..4 {
            print!("|");
            for j in 0..4 {
                let piece = self.get_piece(15 - (i * 4 + j));
                if piece == 0 {
                    print!("   |");
                } else {
                    let piece_char = &format!("{:X}",piece)[2..];
                    print!(" {} |", piece_char);
                }
            }
            println!();
            println!("+---+---+---+---+");
        }
    }

    fn list_actions(&self) -> Vec<Self::Action> {
        let open_index = 15 - self.find_piece_index(0);
        let x = open_index % 4;
        let y = open_index / 4;

        let mut actions = Vec::new();
        if x < 3 {
            actions.push(FifteenPuzzleAction::new(self.get_piece(15 - (open_index + 1))));
        }
        if x > 0 {
            actions.push(FifteenPuzzleAction::new(self.get_piece(15 - (open_index - 1))));
        }
        if y < 3 {
            actions.push(FifteenPuzzleAction::new(self.get_piece(15 - (open_index + 4))));
        }
        if y > 0 {
            actions.push(FifteenPuzzleAction::new(self.get_piece(15 - (open_index - 4))));
        }
        actions
    }

    fn perform_action(&self, action: &Self::Action) -> Self {
        FifteenPuzzle::new(self.move_piece(action.piece))
    }

    fn is_goal_state(&self) -> bool {
        self.board == Self::default_board()
    }

    fn heuristic(&self) -> f64 {
        let mut distance = 0.0;
        for i in 0..16 {
            let piece = self.get_piece(i);
            if piece == 0 { continue; }

            let x = i % 4;
            let y = i / 4;

            let goal_x = (piece) % 4;
            let goal_y = (piece) / 4;

            distance += ((x as i8 - goal_x as i8).abs() + (y as i8 - goal_y as i8).abs()) as f64;
        }
        distance
    }
}

#[derive(PartialEq, Clone)]
pub struct FifteenPuzzleAction {
    pub piece: u8,
}

impl FifteenPuzzleAction {

    pub fn new(piece: u8) -> FifteenPuzzleAction {
        FifteenPuzzleAction { piece }
    }
}

impl ToString for FifteenPuzzleAction {

    fn to_string(&self) -> String {
        format!("{:#x}", self.piece)[2..].to_string()
    }

}

impl FromStr for FifteenPuzzleAction {

    type Err = ();

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        let parsed = u8::from_str_radix(s, 16);
        if parsed.is_err() {
            return Err(());
        }
        Ok(FifteenPuzzleAction { piece: parsed.unwrap() })
    }

}

impl Action for FifteenPuzzleAction {

    fn get_cost(&self) -> f64 {
        1.0
    }

}