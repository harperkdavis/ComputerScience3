// Author: Harper Davis
use std::str::FromStr;

use rand::{rngs::ThreadRng, Rng};

use crate::{state::State, action::Action};

#[derive(Debug, Copy, Clone, PartialEq, Eq, Hash)]
enum Color {
    White = 0,
    Yellow = 1,
    Red = 2,
    Orange = 3,
    Green = 4,
    Blue = 5,
}

impl Color {

    fn to_face(colors: [Color; 8]) -> u32 {
        let mut face = 0_u32;
        for i in 0..8 {
            face |= (colors[i] as u32) << (i * 4);
        }
        face
    }

    // fn from_face(face: u32) -> [Color; 8] {
    //     let mut colors = [Color::White; 8];
    //     for i in 0..8 {
    //         colors[i] = Self::get_color_at(face, i);
    //     }
    //     colors
    // }

    fn get_color_at(face: u32, index: usize) -> Color {
        let color = (face >> (index * 4)) & 0b0111;
        match color {
            0 => Color::White,
            1 => Color::Yellow,
            2 => Color::Red,
            3 => Color::Orange,
            4 => Color::Green,
            5 => Color::Blue,
            _ => unreachable!(),
        }
    }

    fn set_color_at(face: u32, index: usize, color: Color) -> u32 {
        let mut new_face = face;
        new_face &= 0xffffffff - (0b0111 << (index * 4));
        new_face |= (color as u32) << (index * 4);
        new_face
    }

    // fn to_char(&self) -> char {
    //     match self {
    //         Color::White => 'W',
    //         Color::Yellow => 'Y',
    //         Color::Red => 'R',
    //         Color::Orange => 'O',
    //         Color::Green => 'G',
    //         Color::Blue => 'B',
    //     }
    // }

    fn from_index(index: u8) -> Color {
        match index & 0b0111 {
            0 => Color::White,
            1 => Color::Yellow,
            2 => Color::Red,
            3 => Color::Orange,
            4 => Color::Green,
            5 => Color::Blue,
            _ => unreachable!(),
        }
    }

    // fn to_index(&self) -> u8 {
    //     match self {
    //         Color::White => 0,
    //         Color::Yellow => 1,
    //         Color::Red => 2,
    //         Color::Orange => 3,
    //         Color::Green => 4,
    //         Color::Blue => 5,
    //     }
    // }

    fn to_colored(&self, string: &str) -> String {
        match self {
            Color::White => format!("\x1b[37m{}\x1b[0m", string),
            Color::Yellow => format!("\x1b[33m{}\x1b[0m", string),
            Color::Red => format!("\x1b[31m{}\x1b[0m", string),
            Color::Orange => format!("\x1b[35m{}\x1b[0m", string),
            Color::Green => format!("\x1b[32m{}\x1b[0m", string),
            Color::Blue => format!("\x1b[34m{}\x1b[0m", string),
        }
    }
}

#[derive(Clone, PartialEq)]
pub struct CubeState {
    pub faces: [u32; 6],
}

/// This one is going to require a bit of documentation, here goes:
/// The array is a three-dimensional array, with the first dimension being the face,
/// and two other arrays, one for the surrounding faces, and the other for the starting indices of the three colors to rotate (mod 8)
const ROTATIONS: [[[u8; 4]; 2]; 6] = [
    [[3,5,2,4],[0,0,0,0]], // White
    [[2,5,3,4],[4,4,4,4]],
    [[0,5,1,4],[4,6,0,2]],
    [[0,4,1,5],[0,6,0,2]],
    [[0,2,1,3],[6,6,6,2]],
    [[0,3,1,2],[2,6,2,2]],
];

impl CubeState {

    pub fn new() -> Self {
        let mut faces = [0_u32; 6];
        for i in 0..6 {
            faces[i] = Color::to_face([Color::from_index(i as u8); 8]);
        }
        Self { faces }
    }

    pub fn default() -> Self {
        Self { faces: Self::default_cube() }
    }

    pub fn default_cube() -> [u32; 6] {
        [
            Color::to_face([Color::White; 8]), 
            Color::to_face([Color::Yellow; 8]), 
            Color::to_face([Color::Red; 8]), 
            Color::to_face([Color::Orange; 8]), 
            Color::to_face([Color::Green; 8]), 
            Color::to_face([Color::Blue; 8])
        ]
    }

    pub fn scrambled_cube(action_count: u32) -> (Self, Vec<CubeAction>) {
        let mut rng = rand::thread_rng();
        let mut cube = CubeState::default();
        let mut actions = Vec::new();
        for _ in 0..action_count {
            let action = CubeAction::random_move(&mut rng);
            cube = cube.perform_action(&action);
            actions.push(action);
        }
        (cube, actions)
    }

    pub fn rotate_cw(faces: [u32; 6], face: u8) -> [u32; 6] {
        let mut new_faces = faces.clone();
        let face_index = face as usize;

        new_faces[face_index] = faces[face_index].rotate_left(8);

        for i in 0..4 {
            let (from_face_index, from_start_index) = (ROTATIONS[face_index][0][i] as usize, ROTATIONS[face_index][1][i] as usize);
            let (to_face_index, to_start_index) = (ROTATIONS[face_index][0][(i + 1) % 4] as usize, ROTATIONS[face_index][1][(i + 1) % 4] as usize);
            for j in 0..3 {
                let from_color = Color::get_color_at(faces[from_face_index], (from_start_index + j) % 8);
                new_faces[to_face_index] = Color::set_color_at(new_faces[to_face_index], (to_start_index + j) % 8, from_color);
            }
        }

        new_faces
    }

    pub fn rotate_ccw(faces: [u32; 6], face: u8) -> [u32; 6] {
        let mut new_faces = faces.clone();
        let face_index = face as usize;

        new_faces[face_index] = faces[face_index].rotate_right(8);

        for i in 0..4 {
            let (from_face_index, from_start_index) = (ROTATIONS[face_index][0][i] as usize, ROTATIONS[face_index][1][i] as usize);
            let (to_face_index, to_start_index) = (ROTATIONS[face_index][0][(i + 3) % 4] as usize, ROTATIONS[face_index][1][(i + 3) % 4] as usize);
            for j in 0..3 {
                let from_color = Color::get_color_at(faces[from_face_index], (from_start_index + j) % 8);
                new_faces[to_face_index] = Color::set_color_at(new_faces[to_face_index], (to_start_index + j) % 8, from_color);
            }
        }

        new_faces
    }

    pub fn rotate_180(faces: [u32; 6], face: u8) -> [u32; 6] {
        let mut new_faces = faces.clone();
        let face_index = face as usize;

        new_faces[face_index] = faces[face_index].rotate_left(16);

        for i in 0..4 {
            let (from_face_index, from_start_index) = (ROTATIONS[face_index][0][i] as usize, ROTATIONS[face_index][1][i] as usize);
            let (to_face_index, to_start_index) = (ROTATIONS[face_index][0][(i + 2) % 4] as usize, ROTATIONS[face_index][1][(i + 2) % 4] as usize);
            for j in 0..3 {
                let from_color = Color::get_color_at(faces[from_face_index], (from_start_index + j) % 8);
                new_faces[to_face_index] = Color::set_color_at(new_faces[to_face_index], (to_start_index + j) % 8, from_color);
            }
        }

        new_faces
    }
    
}

fn fcol(state: &CubeState, color: Color, index: usize) -> String {
    Color::get_color_at(state.faces[color as usize], index).to_colored("▓")
}

fn ccol(color: Color) -> String {
    color.to_colored("▓")
}

impl ToString for CubeState {

    fn to_string(&self) -> String {
        format!("{:#010x},{:#010x},{:#010x},{:#010x},{:#010x},{:#010x}", self.faces[0], self.faces[1], self.faces[2], self.faces[3], self.faces[4], self.faces[5])
    }

}

impl State for CubeState {
    type Action = CubeAction;

    fn display_pretty(&self) {
        println!("   {}{}{}      \n   {}{}{}      \n   {}{}{}      ", 
            fcol(self, Color::Orange, 4), fcol(self, Color::Orange, 5), fcol(self, Color::Orange, 6),
            fcol(self, Color::Orange, 3), ccol(Color::Orange), fcol(self, Color::Orange, 7),
            fcol(self, Color::Orange, 2), fcol(self, Color::Orange, 1), fcol(self, Color::Orange, 1)
        );
        println!("{}{}{}{}{}{}{}{}{}{}{}{}", 
            fcol(self, Color::Green, 6), fcol(self, Color::Green, 7), fcol(self, Color::Green, 0), 
            fcol(self, Color::White, 0), fcol(self, Color::White, 1), fcol(self, Color::White, 2), 
            fcol(self, Color::Blue, 2), fcol(self, Color::Blue, 3), fcol(self, Color::Blue, 4), 
            fcol(self, Color::Yellow, 4), fcol(self, Color::Yellow, 5), fcol(self, Color::Yellow, 6)
        );
        println!("{}{}{}{}{}{}{}{}{}{}{}{}", 
            fcol(self, Color::Green, 5), ccol(Color::Green), fcol(self, Color::Green, 1),
            fcol(self, Color::White, 7), ccol(Color::White), fcol(self, Color::White, 3),
            fcol(self, Color::Blue, 1), ccol(Color::Blue), fcol(self, Color::Blue, 5),
            fcol(self, Color::Yellow, 3), ccol(Color::Yellow), fcol(self, Color::Yellow, 7)
        );
        println!("{}{}{}{}{}{}{}{}{}{}{}{}", 
            fcol(self, Color::Green, 4), fcol(self, Color::Green, 3), fcol(self, Color::Green, 2),
            fcol(self, Color::White, 6), fcol(self, Color::White, 5), fcol(self, Color::White, 4),
            fcol(self, Color::Blue, 0), fcol(self, Color::Blue, 7), fcol(self, Color::Blue, 6),
            fcol(self, Color::Yellow, 2), fcol(self, Color::Yellow, 1), fcol(self, Color::Yellow, 0)
        );
        println!("   {}{}{}      \n   {}{}{}      \n   {}{}{}      ", 
            fcol(self, Color::Red, 0), fcol(self, Color::Red, 1), fcol(self, Color::Red, 2),
            fcol(self, Color::Red, 7), ccol(Color::Red), fcol(self, Color::Red, 3),
            fcol(self, Color::Red, 6), fcol(self, Color::Red, 5), fcol(self, Color::Red, 4)
        );
    }

    fn is_goal_state(&self) -> bool {
        self.faces == CubeState::default_cube()
    }

    fn list_actions(&self) -> Vec<Self::Action> {
        vec![
            CubeAction::U,
            CubeAction::UPrime,
            CubeAction::U2,
            CubeAction::D,
            CubeAction::DPrime,
            CubeAction::D2,
            CubeAction::R,
            CubeAction::RPrime,
            CubeAction::R2,
            CubeAction::L,
            CubeAction::LPrime,
            CubeAction::L2,
            CubeAction::F,
            CubeAction::FPrime,
            CubeAction::F2,
            CubeAction::B,
            CubeAction::BPrime,
            CubeAction::B2,
        ]
    }

    fn perform_action(&self, action: &Self::Action) -> Self {
        let mut new_faces = self.faces.clone();
        match action {
            CubeAction::U => new_faces = CubeState::rotate_cw(new_faces, 0),
            CubeAction::UPrime => new_faces = CubeState::rotate_ccw(new_faces, 0),
            CubeAction::U2 => new_faces = CubeState::rotate_180(new_faces, 0),
            CubeAction::D => new_faces = CubeState::rotate_cw(new_faces, 1),
            CubeAction::DPrime => new_faces = CubeState::rotate_ccw(new_faces, 1),
            CubeAction::D2 => new_faces = CubeState::rotate_180(new_faces, 1),
            CubeAction::R => new_faces = CubeState::rotate_cw(new_faces, 4),
            CubeAction::RPrime => new_faces = CubeState::rotate_ccw(new_faces, 4),
            CubeAction::R2 => new_faces = CubeState::rotate_180(new_faces, 4),
            CubeAction::L => new_faces = CubeState::rotate_cw(new_faces, 5),
            CubeAction::LPrime => new_faces = CubeState::rotate_ccw(new_faces, 5),
            CubeAction::L2 => new_faces = CubeState::rotate_180(new_faces, 5),
            CubeAction::F => new_faces = CubeState::rotate_cw(new_faces, 2),
            CubeAction::FPrime => new_faces = CubeState::rotate_ccw(new_faces, 2),
            CubeAction::F2 => new_faces = CubeState::rotate_180(new_faces, 2),
            CubeAction::B => new_faces = CubeState::rotate_cw(new_faces, 3),
            CubeAction::BPrime => new_faces = CubeState::rotate_ccw(new_faces, 3),
            CubeAction::B2 => new_faces = CubeState::rotate_180(new_faces, 3),
        }
        CubeState { faces: new_faces }
    }

    fn heuristic(&self) -> f64 {
        // 3d manhattan
        0.0
    }

}

#[derive(Debug, Clone, PartialEq)]
pub enum CubeAction {
    U, UPrime, U2,
    D, DPrime, D2,
    R, RPrime, R2,
    L, LPrime, L2,
    F, FPrime, F2,
    B, BPrime, B2,
}

impl ToString for CubeAction {

    fn to_string(&self) -> String {
        match self {
            CubeAction::U => "U".to_string(),
            CubeAction::UPrime => "U'".to_string(),
            CubeAction::U2 => "U2".to_string(),
            CubeAction::D => "D".to_string(),
            CubeAction::DPrime => "D'".to_string(),
            CubeAction::D2 => "D2".to_string(),
            CubeAction::R => "R".to_string(),
            CubeAction::RPrime => "R'".to_string(),
            CubeAction::R2 => "R2".to_string(),
            CubeAction::L => "L".to_string(),
            CubeAction::LPrime => "L'".to_string(),
            CubeAction::L2 => "L2".to_string(),
            CubeAction::F => "F".to_string(),
            CubeAction::FPrime => "F'".to_string(),
            CubeAction::F2 => "F2".to_string(),
            CubeAction::B => "B".to_string(),
            CubeAction::BPrime => "B'".to_string(),
            CubeAction::B2 => "B2".to_string(),
        }
    }

}

impl FromStr for CubeAction {

    type Err = ();

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s {
            "U" => Ok(CubeAction::U),
            "U'" => Ok(CubeAction::UPrime),
            "D" => Ok(CubeAction::D),
            "D'" => Ok(CubeAction::DPrime),
            "R" => Ok(CubeAction::R),
            "R'" => Ok(CubeAction::RPrime),
            "L" => Ok(CubeAction::L),
            "L'" => Ok(CubeAction::LPrime),
            "F" => Ok(CubeAction::F),
            "F'" => Ok(CubeAction::FPrime),
            "B" => Ok(CubeAction::B),
            "B'" => Ok(CubeAction::BPrime),
            _ => Err(()),
        }
    }

}

impl Action for CubeAction {

    fn get_cost(&self) -> f64 {
        1.0
    }

}

impl CubeAction {

    fn random_move(rng: &mut ThreadRng) -> CubeAction {
        let random = rng.gen_range(0..18);
        match random {
            0 => CubeAction::U,
            1 => CubeAction::UPrime,
            2 => CubeAction::D,
            3 => CubeAction::DPrime,
            4 => CubeAction::R,
            5 => CubeAction::RPrime,
            6 => CubeAction::L,
            7 => CubeAction::LPrime,
            8 => CubeAction::F,
            9 => CubeAction::FPrime,
            10 => CubeAction::B,
            11 => CubeAction::BPrime,
            12 => CubeAction::U2,
            13 => CubeAction::D2,
            14 => CubeAction::R2,
            15 => CubeAction::L2,
            16 => CubeAction::F2,
            17 => CubeAction::B2,
            _ => panic!("Random number out of range"),
        }
    }

}