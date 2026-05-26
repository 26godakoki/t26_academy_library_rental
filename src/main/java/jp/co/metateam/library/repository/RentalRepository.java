package jp.co.metateam.library.repository;

import java.util.List; // 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import jp.co.metateam.library.model.RentalManage;

@Repository
public interface RentalRepository extends JpaRepository<RentalManage, Long> { // ※Longの部分は実際のIDの型に合わせてください
    
    /**
     * 在庫IDと複数のステータス（リスト）を指定してレコードを全件取得する
     */
    List<RentalManage> findByStockIdAndStatusIn(String stockId, List<Integer> statuses);

}