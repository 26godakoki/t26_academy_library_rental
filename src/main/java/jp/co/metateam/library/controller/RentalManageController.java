package jp.co.metateam.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.StockService;
import jp.co.metateam.library.service.RentalService; // 
import jp.co.metateam.library.values.RentalStatus;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
public class RentalManageController {

    private final AccountService accountService;
    private final StockService stockService;
    private final RentalService rentalService;

    @Autowired
    public RentalManageController(AccountService accountService,
            StockService stockService,
            RentalService rentalService) {
        this.accountService = accountService;
        this.stockService = stockService;
        this.rentalService = rentalService;
    }

    /**
     * 一覧表示
     */
    @GetMapping("/rental/index")
    public String index(Model model) {
        model.addAttribute("list", rentalService.findAll());
        return "rental/index";
    }

    /**
     * 登録画面表示
     */
    @GetMapping("/rental/add")
    public String add(Model model) {
        model.addAttribute("rentalManageDto", new RentalManageDto());
        model.addAttribute("accounts", accountService.findAll());
        model.addAttribute("stockList", stockService.findAll());
        model.addAttribute("rentalStatus", RentalStatus.values());

        return "rental/add";
    }

    /**
     * 登録処理（一本化・修正完了）
     */
    @PostMapping("/rental/add")
    public String register(
            @Valid @ModelAttribute("rentalManageDto") RentalManageDto dto,
            BindingResult result,
            Model model) {

        // 1. DTO単体のチェック（必須入力、キャンセル・返却済み制限、日付の前後チェックなど）
        if (result.hasErrors()) {
            log.error("DTOバリデーションエラー: {}", result);
            // エラー時はセレクトボックスなどの再表示用データを詰める
            setupFormModel(model);
            return "rental/add";
        }

        // 2. 作成したServiceのロジックを呼び出す（在庫状態チェック ＆ 重複チェック ＆ 保存）
        boolean isSaved = rentalService.checkAndSaveRental(dto, result);
        
        // 3. 在庫エラーや期間重複エラーが発生した場合の処理
        if (!isSaved) {
            log.warn("ビジネスロジックエラー（在庫または重複）により登録を中断します");
            // エラーメッセージを表示した状態で入力画面に戻す
            setupFormModel(model);
            return "rental/add";
        }

        // 4. 重複もなく無事に保存できたら一覧へリダイレクト
        return "redirect:/rental/index";
    }

    /**
     * 登録画面で必要なマスタ・リストデータをModelにセットする共通メソッド
     */
    private void setupFormModel(Model model) {
        model.addAttribute("accounts", accountService.findAll());
        model.addAttribute("stockList", stockService.findAll());
        model.addAttribute("rentalStatus", RentalStatus.values());
    }
}