package com.trove.ticket_trove.dto.seatGrade.response;

import com.trove.ticket_trove.model.entity.seat_grade.SeatGradeEntity;

public record SeatGradeInfoResponse(
        String grade,
        Integer price,
        Integer totalSeat) {

    public static SeatGradeInfoResponse from(
            SeatGradeEntity seatGradeEntity) {

        return new SeatGradeInfoResponse(
                seatGradeEntity.getGrade(),
                seatGradeEntity.getPrice(),
                seatGradeEntity.getTotalSeat());
    }

}