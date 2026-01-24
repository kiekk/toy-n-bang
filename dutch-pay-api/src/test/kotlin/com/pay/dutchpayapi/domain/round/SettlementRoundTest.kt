package com.pay.dutchpayapi.domain.round

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class SettlementRoundTest {

    @Test
    @DisplayName("정산 라운드를 생성할 수 있다")
    fun create() {
        // given
        val title = "1차 고기집"
        val amount = BigDecimal("30000")
        val payerId = 1L
        val gatheringId = 1L

        // when
        val round = SettlementRound(
            title = title,
            amount = amount,
            payerId = payerId,
            gatheringId = gatheringId
        )

        // then
        assertThat(round.title).isEqualTo(title)
        assertThat(round.amount).isEqualByComparingTo(amount)
        assertThat(round.payerId).isEqualTo(payerId)
        assertThat(round.gatheringId).isEqualTo(gatheringId)
        assertThat(round.receiptImageUrl).isNull()
    }

    @Test
    @DisplayName("라운드 정보를 수정할 수 있다")
    fun update() {
        // given
        val round = SettlementRound(
            title = "원래 제목",
            amount = BigDecimal("10000"),
            payerId = 1L,
            gatheringId = 1L
        )

        val newTitle = "수정된 제목"
        val newAmount = BigDecimal("50000")
        val newPayerId = 2L

        // when
        round.update(newTitle, newAmount, newPayerId)

        // then
        assertThat(round.title).isEqualTo(newTitle)
        assertThat(round.amount).isEqualByComparingTo(newAmount)
        assertThat(round.payerId).isEqualTo(newPayerId)
    }

    @Test
    @DisplayName("영수증 이미지 URL을 설정할 수 있다")
    fun updateReceiptImageUrl() {
        // given
        val round = SettlementRound(
            title = "1차",
            amount = BigDecimal("10000"),
            payerId = 1L,
            gatheringId = 1L
        )

        val imageUrl = "/uploads/receipt.jpg"

        // when
        round.updateReceiptImageUrl(imageUrl)

        // then
        assertThat(round.receiptImageUrl).isEqualTo(imageUrl)
    }

    @Test
    @DisplayName("영수증 이미지 URL을 null로 설정할 수 있다")
    fun updateReceiptImageUrlToNull() {
        // given
        val round = SettlementRound(
            title = "1차",
            amount = BigDecimal("10000"),
            payerId = 1L,
            gatheringId = 1L
        )
        round.updateReceiptImageUrl("/uploads/receipt.jpg")

        // when
        round.updateReceiptImageUrl(null)

        // then
        assertThat(round.receiptImageUrl).isNull()
    }
}
