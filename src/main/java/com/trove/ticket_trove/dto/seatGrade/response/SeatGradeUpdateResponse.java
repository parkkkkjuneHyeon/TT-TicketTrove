package com.trove.ticket_trove.dto.seatGrade.response;

import com.trove.ticket_trove.model.entity.seat_grade.SeatGradeEntity;

public record SeatGradeUpdateResponse(
        String grade,
        Integer price,
        Integer totalSeat) {

    public static SeatGradeUpdateResponse from(
            SeatGradeEntity seatGradeEntity) {

        return new SeatGradeUpdateResponse(
                seatGradeEntity.getGrade(),
                seatGradeEntity.getPrice(),
                seatGradeEntity.getTotalSeat());
    }
}
