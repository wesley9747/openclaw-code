from pydantic import BaseModel
from typing import List, Optional

class TicketParseResponse(BaseModel):
    ticket_id: str
    red_balls: List[str]  # ["03", "07", ...]
    blue_ball: str
    period: Optional[str] = None
    lottery_type: Optional[str] = None

class RecordCreate(BaseModel):
    period: str
    lottery_type: str
    red_balls: List[str]
    blue_ball: str
    bet_amount: float = 2.0
    note: Optional[str] = None

class RecordResponse(BaseModel):
    id: int
    period: str
    lottery_type: str
    red_balls: List[str]
    blue_ball: str
    draw_red_balls: Optional[List[str]] = None
    draw_blue_ball: Optional[str] = None
    is_win: bool = False
    prize_level: Optional[str] = None
    prize_amount: float = 0.0
    bet_amount: float
    note: Optional[str] = None
    created_at: str
