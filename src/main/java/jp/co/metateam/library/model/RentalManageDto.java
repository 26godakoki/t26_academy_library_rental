package jp.co.metateam.library.model;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import jp.co.metateam.library.values.RentalStatus;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
public class RentalManageDto {

    @NotBlank(message = "社員番号は必須です")
    private String employeeId;

    @NotBlank(message = "在庫管理番号は必須です")
    private String stockId;

    @NotNull(message = "ステータスは必須です")
    private Integer status;

    @NotNull(message = "貸出予定日は必須です")
    private LocalDate expectedRentalOn;

    @NotNull(message = "返却予定日は必須です")
    private LocalDate expectedReturnOn;

    /**
     * 日付の整合性チェック（返却予定日＞貸出予定日）
     */
    @AssertTrue(message = "返却予定日は貸出予定日より後にしてください")
    public boolean isValidDateRange() {
        if (expectedRentalOn == null || expectedReturnOn == null) {
            return true;
        }
        return expectedReturnOn.isAfter(expectedRentalOn);
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDate getExpectedRentalOn() {
        return expectedRentalOn;
    }

    public void setExpectedRentalOn(LocalDate expectedRentalOn) {
        this.expectedRentalOn = expectedRentalOn;
    }

    public LocalDate getExpectedReturnOn() {
        return expectedReturnOn;
    }

    public void setExpectedReturnOn(LocalDate expectedReturnOn) {
        this.expectedReturnOn = expectedReturnOn;
    }

    /**
     * 1. 【目的のエラー】「キャンセル」か「返却済み」が選択された場合のエラー表示
     */
    @AssertTrue(message = "「キャンセル」か「返却済み」は選択できません")
    public boolean isNotCancelledOrReturned() {
        if (status == null) {
            return true;
        }
        // ステータスが「キャンセル(2)」または「返却済み(3)」の場合はエラー（false）
        if (status == 2 || status == 3) {
            return false;
        }
        return true;
    }

    /**
     *  貸出待ち(0)・貸出中(1)以外の「その他不適切な値」を弾くチェック
     * ※「キャンセル」「返却済み」の時は、1番目のチェックでエラーを出すので、ここでは重複させないためにtrue
     */
    @AssertTrue(message = "貸出ステータスは『貸出待ち』または『貸出中』を選択してください")
    public boolean isValidStatus() {
        if (status == null) {
            return true;
        }
        // キャンセル(2)、返却済み(3)の場合はここではスルー
        if (status == 2 || status == 3) {
            return true;
        }
        return status == 0 || status == 1;
    }

    /**
     * 貸出ステータスと貸出予定日の組み合わせが正しいかチェック
     */
    @AssertTrue(message = "貸出ステータスと貸出予定日の組み合わせが正しくありません")
    public boolean isValidStatusAndDate() {
        if (status == null || expectedRentalOn == null) {
            return true;
        }

        LocalDate today = LocalDate.now();

        // 【条件1】「貸出待ち」の場合：貸出予定日が未来日（明日以降）であること
        if (status == 0) {
            return expectedRentalOn.isAfter(today);
        }

        // 【条件2】「貸出中」の場合：貸出予定日が本日、または過去日であること
        if (status == 1) {
            return expectedRentalOn.isEqual(today) || expectedRentalOn.isBefore(today);
        }

        // ステータスが 0(貸出待ち)・1(貸出中) 以外（キャンセルや返却済みなど）の場合は、
        // 日付の組み合わせチェック自体は関係がないため、一律でスルー（true）
        return true;
    }
}