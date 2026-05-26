package jp.co.metateam.library.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.repository.RentalRepository;
import jp.co.metateam.library.repository.StockRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class RentalService {

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private RentalRepository rentalRepository; 

    /**
     * 貸出情報をデータベース（DB）に保存する
     */
    @Transactional
    public void register(RentalManageDto dto) {
        RentalManage rental = new RentalManage();

        rental.setExpectedRentalOn(dto.getExpectedRentalOn());
        rental.setExpectedReturnOn(dto.getExpectedReturnOn());
        rental.setEmployeeId(dto.getEmployeeId());
        rental.setStockId(dto.getStockId());
        rental.setStatus(dto.getStatus());

        rentalRepository.save(rental);
    }

    public List<RentalManage> findAll() {
        return rentalRepository.findAll();
    }

    /**
     * 貸出登録のバリデーションおよび保存処理
     */
    public boolean checkAndSaveRental(RentalManageDto dto, BindingResult result) {
        
        // 在庫の保管状態チェック
        var stock = stockRepository.findById(dto.getStockId()).orElse(null);
        
        if (stock == null || !"利用可".equals(stock.getStatus())) {
            result.rejectValue("stockId", "error.stockStatus", "選択された在庫管理番号の保管状態が「利用可」ではないため、貸出できません");
            return false; 
        }

        // 貸出待ち・貸出中のレコードを全件取得
        List<RentalManage> activeRentals = rentalRepository.findByStockIdAndStatusIn(
            dto.getStockId(), 
            List.of(0, 1) // 0:貸出待ち, 1:貸出中
        );

        // 重複チェックと保存処理
        if (activeRentals.isEmpty()) {
            // 取得したレコードが0件だった場合、そのままDBに保存
            register(dto); 
            return true;
        } else {
            // 0件出なかった場合、ループ開始
            LocalDate newStart = dto.getExpectedRentalOn(); 
            LocalDate newEnd = dto.getExpectedReturnOn();  

            for (RentalManage existingRental : activeRentals) {
                LocalDate existStart = existingRental.getExpectedRentalOn(); 
                LocalDate existEnd = existingRental.getExpectedReturnOn();   

                // 条件：貸出予定日 ＞ リスト返却予定日 or 返却予定日 ＜ リスト貸出予定日（重複していない条件）
                boolean isSafe = newStart.isAfter(existEnd) || newEnd.isBefore(existStart);

                if (!isSafe) {
                    // 重複していた場合、エラーを表示
                    result.rejectValue("expectedRentalOn", "error.duplicate", "指定された期間は、すでに「貸出待ち」または「貸出中」の予約と重複しています");
                    return false; 
                }
            }

            // 全件のリストをループさせ、重複していなかったら保存
            register(dto);
            return true;
        }
    }
}