// Author: Harper Davis

pub trait State: Clone + ToString + PartialEq {
    type Action;

    fn display_pretty(&self);
    fn list_actions(&self) -> Vec<Self::Action>;
    fn perform_action(&self, action: &Self::Action) -> Self;
    fn is_goal_state(&self) -> bool;

    fn heuristic(&self) -> f64;
}
