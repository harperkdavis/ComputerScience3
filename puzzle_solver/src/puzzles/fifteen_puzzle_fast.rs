// Author: Harper Davis
// Unused
use crate::state::State;

use super::fifteen_puzzle::FifteenPuzzleAction;

#[derive(PartialEq, Clone)]
pub struct FifteenPuzzleFast {
    board_index: u64,
    board_pieces: u64,
}

impl FifteenPuzzleFast {
    
    pub fn new(board_index: u64, board_pieces: u64) -> Self {
        FifteenPuzzleFast { board_index, board_pieces }
    }

    pub fn from_pieces(board_pieces: u64) -> Self {
        let mut board_index = 0;
        for i in 0..16 {
            let piece = (board_pieces >> (i * 4)) & 0b1111;
            board_index |= i << (piece * 4);
        }
        FifteenPuzzleFast { board_index, board_pieces }
    }

    pub fn default() -> Self {
        FifteenPuzzleFast { board_index: Self::default_board(), board_pieces: Self::default_board() }
    }

    fn default_board() -> u64 {
        0xfedcba9876543210
    }

    fn move_piece(&self, piece: u8) -> Self {
        let piece_index = (self.board_index >> (piece * 4)) & 0b1111;
        let open_index = (self.board_index) & 0b1111;

        let new_board_pieces = self.board_pieces & !(0b1111 << (piece_index * 4)) & !(0b1111 << (open_index * 4)) | (piece as u64) << (open_index * 4);
        let new_board_index = self.board_index & !(0b1111 << (piece * 4)) & !(0b1111) | open_index << (piece * 4) | piece_index;

        FifteenPuzzleFast { board_index: new_board_index, board_pieces: new_board_pieces }
    }

}

impl ToString for FifteenPuzzleFast {

    fn to_string(&self) -> String {
        format!("{:#018x}/{:#018x}", self.board_pieces, self.board_index)
    }

}

impl State for FifteenPuzzleFast {
    type Action = FifteenPuzzleAction;

    fn display_pretty(&self) {
        println!("+---+---+---+---+");
        for i in 0..4 {
            print!("|");
            for j in 0..4 {
                let piece = self.board_pieces >> (i * 4 + j * 16) & 0b1111;
                if piece == 0 {
                    print!("   |");
                } else {
                    let piece_char = &format!("{:X}", piece)[2..];
                    print!(" {} |", piece_char);
                }
            }
            println!();
            println!("+---+---+---+---+");
        }
    }

    fn list_actions(&self) -> Vec<Self::Action> {
        let mut actions = Vec::new();
        let open_index = (self.board_index) & 0b1111;
        if open_index > 3 {
            actions.push(FifteenPuzzleAction::new((self.board_pieces >> ((open_index - 4) * 4) & 0b1111) as u8));
        }
        if open_index < 12 {
            actions.push(FifteenPuzzleAction::new((self.board_pieces >> ((open_index + 4) * 4) & 0b1111) as u8));
        }
        if open_index % 4 > 0 {
            actions.push(FifteenPuzzleAction::new((self.board_pieces >> ((open_index - 1) * 4) & 0b1111) as u8));
        }
        if open_index % 4 < 3 {
            actions.push(FifteenPuzzleAction::new((self.board_pieces >> ((open_index + 1) * 4) & 0b1111) as u8));
        }
        actions
    }

    fn heuristic(&self) -> f64 {
        let mut manhattan = 0.0;
        for i in 0..16 {
            let pos = ((self.board_index >> (i * 4)) & 0b1111) as u8;
            
            let goal_x = i % 4;
            let goal_y = i / 4;

            let x = pos % 4;
            let y = pos / 4;

            manhattan += ((goal_x as i8 - x as i8).abs() + (goal_y as i8 - y as i8).abs()) as f64;
        }

        manhattan
    }

    fn is_goal_state(&self) -> bool {
        self.board_pieces == Self::default_board()
    }

    fn perform_action(&self, action: &Self::Action) -> Self {
        self.move_piece(action.piece)    
    }
}