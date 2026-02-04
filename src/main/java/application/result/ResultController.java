package application.result;

import application.SceneManager;
import application.model.DisplayStatistics;
import application.model.FilteredResult;
import application.model.MistakeDetail;
import application.model.MistakeDetailRow;
import application.model.MistakeStatsRow;
import application.model.MistakeType;
import application.model.StatsTableRow;
import application.model.TestResult;
import application.model.WeaknessAnalysisResult;
import application.model.WeaknessCharRow;
import application.model.WeaknessPairRow;
import application.model.WordResult;
import application.model.Period;
import application.service.UserDataManager;
import application.service.WeaknessAnalyzer;
import application.service.WeaknessAnalyzer.SubstitutionPair;
import application.service.WeaknessAnalyzer.TranspositionPair;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.TableCell;
import javafx.scene.text.Text;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 結果表示画面を管理するコントローラクラス
 */

public class ResultController {
    private static final int WEAKNESS_ANALYSIS_LIMIT = 3;

    @FXML private Button toHomeButton;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private StackPane loadingOverlay;
    @FXML private HBox weaknessAnalysisHBox;

    @FXML private ChoiceBox<Period> periodChoiceBox;
    @FXML private BarChart<String, Integer> barChart;
    private XYChart.Series<String, Integer> mistakeSeries = new XYChart.Series<>();
    @FXML private TableView<MistakeStatsRow> mistakeStatsTable;
    @FXML private TableColumn<MistakeStatsRow, String> mistakeTypeColumn;
    @FXML private TableColumn<MistakeStatsRow, Integer> mistakeCountColumn;

    @FXML private TableView<WeaknessPairRow> substitutionTableView;
    @FXML private TableColumn<WeaknessPairRow, String> subPairColumn;
    @FXML private TableColumn<WeaknessPairRow, Long> subCountColumn;
    @FXML private TableView<WeaknessPairRow> transpositionTableView;
    @FXML private TableColumn<WeaknessPairRow, String> transPairColumn;
    @FXML private TableColumn<WeaknessPairRow, Long> transCountColumn;
    @FXML private TableView<WeaknessCharRow> deletionTableView;
    @FXML private TableColumn<WeaknessCharRow, Character> delCharColumn;
    @FXML private TableColumn<WeaknessCharRow, Long> delCountColumn;
    @FXML private TableView<WeaknessPairRow> insertionTableView;
    @FXML private TableColumn<WeaknessPairRow, String> insPairColumn;
    @FXML private TableColumn<WeaknessPairRow, Long> insCountColumn;

    @FXML private LineChart<String, Number> wpmLineChart;
    private XYChart.Series<String, Number> wpmSeries;
    @FXML private TableView<StatsTableRow> wpmStatsTable;
    @FXML private TableColumn<StatsTableRow, String> wpmPeriodColumn;
    @FXML private TableColumn<StatsTableRow, Number> wpmDataColumn;

    @FXML private LineChart<String, Number> accuracyLineChart;
    private XYChart.Series<String, Number> accuracySeries;
    @FXML private TableView<StatsTableRow> accuracyTable;
    @FXML private TableColumn<StatsTableRow, String> accuracyPeriodColumn;
    @FXML private TableColumn<StatsTableRow, Number> accuracyDataColumn;

    private Map<MistakeType, Integer> lastMistakeStats;
    private Map<MistakeType, Integer> todayMistakeStats;
    private Map<MistakeType, Integer> thisWeekMistakeStats;
    private Map<MistakeType, Integer> thisMonthMistakeStats;
    private Map<MistakeType, Integer> allTimeMistakeStats;

    private WeaknessAnalysisResult lastAnalysis;
    private WeaknessAnalysisResult todayAnalysis;
    private WeaknessAnalysisResult thisWeekAnalysis;
    private WeaknessAnalysisResult thisMonthAnalysis;
    private WeaknessAnalysisResult allTimeAnalysis;

    //以下のフィールドは誤答リスト用
    @FXML private Accordion mistakeListAccordion;
    @FXML private TitledPane lastMistakeListPane;

    @FXML private TableView<MistakeDetailRow> lastMistakesTable;
    @FXML private TableView<MistakeDetailRow> todayMistakesTable;
    @FXML private TableView<MistakeDetailRow> thisWeekMistakesTable;
    @FXML private TableView<MistakeDetailRow> thisMonthMistakesTable;
    @FXML private TableView<MistakeDetailRow> allTimeMistakesTable;

    //前回
    @FXML private TableColumn<MistakeDetailRow, String> lastQuestionColumn;
    @FXML private TableColumn<MistakeDetailRow, String> lastAnswerColumn;
    @FXML private TableColumn<MistakeDetailRow, Integer> lastSubColumn;
    @FXML private TableColumn<MistakeDetailRow, Integer> lastTransColumn;
    @FXML private TableColumn<MistakeDetailRow, Integer> lastDelColumn;
    @FXML private TableColumn<MistakeDetailRow, Integer> lastInsColumn;

    //今日
    @FXML private TableColumn<MistakeDetailRow, String> todayQuestionColumn;
    @FXML private TableColumn<MistakeDetailRow, String> todayAnswerColumn;
    @FXML private TableColumn<MistakeDetailRow, Integer> todaySubColumn;
    @FXML private TableColumn<MistakeDetailRow, Integer> todayTransColumn;
    @FXML private TableColumn<MistakeDetailRow, Integer> todayDelColumn;
    @FXML private TableColumn<MistakeDetailRow, Integer> todayInsColumn;

    //今週
    @FXML private TableColumn<MistakeDetailRow, String> thisWeekQuestionColumn;
    @FXML private TableColumn<MistakeDetailRow, String> thisWeekAnswerColumn;
    @FXML private TableColumn<MistakeDetailRow, Integer> thisWeekSubColumn;
    @FXML private TableColumn<MistakeDetailRow, Integer> thisWeekTransColumn;
    @FXML private TableColumn<MistakeDetailRow, Integer> thisWeekDelColumn;
    @FXML private TableColumn<MistakeDetailRow, Integer> thisWeekInsColumn;

    //今月
    @FXML private TableColumn<MistakeDetailRow, String> thisMonthQuestionColumn;
    @FXML private TableColumn<MistakeDetailRow, String> thisMonthAnswerColumn;
    @FXML private TableColumn<MistakeDetailRow, Integer> thisMonthSubColumn;
    @FXML private TableColumn<MistakeDetailRow, Integer> thisMonthTransColumn;
    @FXML private TableColumn<MistakeDetailRow, Integer> thisMonthDelColumn;
    @FXML private TableColumn<MistakeDetailRow, Integer> thisMonthInsColumn;

    //全期間
    @FXML private TableColumn<MistakeDetailRow, String> allTimeQuestionColumn;
    @FXML private TableColumn<MistakeDetailRow, String> allTimeAnswerColumn;
    @FXML private TableColumn<MistakeDetailRow, Integer> allTimeSubColumn;
    @FXML private TableColumn<MistakeDetailRow, Integer> allTimeTransColumn;
    @FXML private TableColumn<MistakeDetailRow, Integer> allTimeDelColumn;
    @FXML private TableColumn<MistakeDetailRow, Integer> allTimeInsColumn;

    private record ProcessedData(
        FilteredResult filteredResult,
        DisplayStatistics statistics,
        Map<MistakeType, Integer> lastMistakeStats,
        Map<MistakeType, Integer> todayMistakeStats,
        Map<MistakeType, Integer> thisWeekMistakeStats,
        Map<MistakeType, Integer> thisMonthMistakeStats,
        Map<MistakeType, Integer> allTimeMistakeStats,
        WeaknessAnalysisResult lastAnalysisResult,
        WeaknessAnalysisResult todayAnalysisResult,
        WeaknessAnalysisResult thisWeekAnalysisResult,
        WeaknessAnalysisResult thisMonthAnalysisResult,
        WeaknessAnalysisResult allTimeAnalysisResult
    ) {}
    
    @FXML
    private void switchResultToHome(ActionEvent event) {
        SceneManager.switchSceneByFileName("/application/home/home.fxml");
    }

    /**
     * 各種データを計算し、画面に反映するためのメソッド
     */
    @FXML
    private void initialize() {
        //画面をロード中の状態にする
        loadingOverlay.setVisible(true);
        loadingIndicator.setVisible(true);
        loadingOverlay.setMouseTransparent(false);

        //各種テーブルの列の設定
        //以下ミスの統計用
        setupBarChart();
        setupWeaknessPairTableColumn(subPairColumn, subCountColumn);
        setupWeaknessPairTableColumn(transPairColumn, transCountColumn);
        setupWeaknessCharacterTableColumn(delCharColumn, delCountColumn);
        setupWeaknessPairTableColumn(insPairColumn, insCountColumn);
        setupMistakeStatsTable(mistakeTypeColumn, mistakeCountColumn);

        //以下誤答リスト用
        setupDetailTableColumns(lastQuestionColumn, lastAnswerColumn, lastSubColumn, lastTransColumn, lastDelColumn, lastInsColumn);
        setupDetailTableColumns(todayQuestionColumn, todayAnswerColumn, todaySubColumn, todayTransColumn, todayDelColumn, todayInsColumn);
        setupDetailTableColumns(thisWeekQuestionColumn, thisWeekAnswerColumn, thisWeekSubColumn, thisWeekTransColumn, thisWeekDelColumn, thisWeekInsColumn);
        setupDetailTableColumns(thisMonthQuestionColumn, thisMonthAnswerColumn, thisMonthSubColumn, thisMonthTransColumn, thisMonthDelColumn, thisMonthInsColumn);
        setupDetailTableColumns(allTimeQuestionColumn, allTimeAnswerColumn, allTimeSubColumn, allTimeTransColumn, allTimeDelColumn, allTimeInsColumn);

        /**
         * 重いデータ読み込みと処理を非同期処理で行う
         * 画面が固まるのを防ぐためにメインスレッドとは別スレッドで行う
         */
        Task<ProcessedData> loadDataTask = new Task<>() {
            @Override
            protected ProcessedData call() throws Exception {
                //このメソッド内はバックグラウンドスレッドで実行される
                FilteredResult filteredResult = UserDataManager.getInstance().getFilteredResult(); //結果を読み込み、各期間ごとにフィルタリング
                DisplayStatistics statistics = StatisticsService.createDisplayStatistics(filteredResult); //各期間の統計データ(WPM, 正答率)を計算する

                //各ミスの種類ごとの統計を計算する
                Map<MistakeType, Integer> lastMistakeStats = calculateMistakeStats(filteredResult.lastResult() != null ? List.of(filteredResult.lastResult()) : Collections.emptyList());
                Map<MistakeType, Integer> todayMistakeStats = calculateMistakeStats(filteredResult.todayResults());
                Map<MistakeType, Integer> thisWeekMistakeStats = calculateMistakeStats(filteredResult.thisWeekResults());
                Map<MistakeType, Integer> thisMonthMistakeStats = calculateMistakeStats(filteredResult.thisMonthResults());
                Map<MistakeType, Integer> allTimeMistakeStats = calculateMistakeStats(filteredResult.allResults());

                //各ミスの種類ごとに起こりやすいミスを分析する
                WeaknessAnalyzer lastWeaknessAnalyzer = new WeaknessAnalyzer(filteredResult.lastResult() != null ? List.of(filteredResult.lastResult()) : Collections.emptyList());
                WeaknessAnalyzer todayWeaknessAnalyzer = new WeaknessAnalyzer(filteredResult.todayResults());
                WeaknessAnalyzer thisWeekWeaknessAnalyzer = new WeaknessAnalyzer(filteredResult.thisWeekResults());
                WeaknessAnalyzer thisMonthWeaknessAnalyzer = new WeaknessAnalyzer(filteredResult.thisMonthResults());
                WeaknessAnalyzer allTimeWeaknessAnalyzer = new WeaknessAnalyzer(filteredResult.allResults());

                WeaknessAnalysisResult lastAnalysis = new WeaknessAnalysisResult(
                    lastWeaknessAnalyzer.findTopSubstitutionMistakes(WEAKNESS_ANALYSIS_LIMIT),
                    lastWeaknessAnalyzer.findTopTranspositionMistakes(WEAKNESS_ANALYSIS_LIMIT),
                    lastWeaknessAnalyzer.findTopDeletionMistakes(WEAKNESS_ANALYSIS_LIMIT),
                    lastWeaknessAnalyzer.findTopInsertionMistakes(WEAKNESS_ANALYSIS_LIMIT)
                );
                WeaknessAnalysisResult todayAnalysis = new WeaknessAnalysisResult(
                    todayWeaknessAnalyzer.findTopSubstitutionMistakes(WEAKNESS_ANALYSIS_LIMIT),
                    todayWeaknessAnalyzer.findTopTranspositionMistakes(WEAKNESS_ANALYSIS_LIMIT),
                    todayWeaknessAnalyzer.findTopDeletionMistakes(WEAKNESS_ANALYSIS_LIMIT),
                    todayWeaknessAnalyzer.findTopInsertionMistakes(WEAKNESS_ANALYSIS_LIMIT)
                );
                WeaknessAnalysisResult thisWeekAnalysis = new WeaknessAnalysisResult(
                    thisWeekWeaknessAnalyzer.findTopSubstitutionMistakes(WEAKNESS_ANALYSIS_LIMIT),
                    thisWeekWeaknessAnalyzer.findTopTranspositionMistakes(WEAKNESS_ANALYSIS_LIMIT),
                    thisWeekWeaknessAnalyzer.findTopDeletionMistakes(WEAKNESS_ANALYSIS_LIMIT),
                    thisWeekWeaknessAnalyzer.findTopInsertionMistakes(WEAKNESS_ANALYSIS_LIMIT)
                );
                WeaknessAnalysisResult thisMonthAnalysis = new WeaknessAnalysisResult(
                    thisMonthWeaknessAnalyzer.findTopSubstitutionMistakes(WEAKNESS_ANALYSIS_LIMIT),
                    thisMonthWeaknessAnalyzer.findTopTranspositionMistakes(WEAKNESS_ANALYSIS_LIMIT),
                    thisMonthWeaknessAnalyzer.findTopDeletionMistakes(WEAKNESS_ANALYSIS_LIMIT),
                    thisMonthWeaknessAnalyzer.findTopInsertionMistakes(WEAKNESS_ANALYSIS_LIMIT)
                );
                WeaknessAnalysisResult allTimeAnalysis = new WeaknessAnalysisResult(
                    allTimeWeaknessAnalyzer.findTopSubstitutionMistakes(WEAKNESS_ANALYSIS_LIMIT),
                    allTimeWeaknessAnalyzer.findTopTranspositionMistakes(WEAKNESS_ANALYSIS_LIMIT),
                    allTimeWeaknessAnalyzer.findTopDeletionMistakes(WEAKNESS_ANALYSIS_LIMIT),
                    allTimeWeaknessAnalyzer.findTopInsertionMistakes(WEAKNESS_ANALYSIS_LIMIT)
                );

                //処理したデータをすべてProcessDataレコードにまとめてTaskの結果として返す
                return new ProcessedData(filteredResult,
                    statistics,
                    lastMistakeStats,
                    todayMistakeStats,
                    thisWeekMistakeStats,
                    thisMonthMistakeStats,
                    allTimeMistakeStats,
                    lastAnalysis,
                    todayAnalysis,
                    thisWeekAnalysis,
                    thisMonthAnalysis,
                    allTimeAnalysis
                    );
            }
        };

        //Taskが正常終了した場合の処理
        loadDataTask.setOnSucceeded(event -> {
            //これはJavaFX Application Threadで行われる  これはわからない
            ProcessedData data = loadDataTask.getValue();

            //計算結果をフィールドに保持
            this.lastMistakeStats = data.lastMistakeStats();
            this.todayMistakeStats = data.todayMistakeStats();
            this.thisWeekMistakeStats = data.thisWeekMistakeStats();
            this.thisMonthMistakeStats = data.thisMonthMistakeStats();
            this.allTimeMistakeStats = data.allTimeMistakeStats();
            this.lastAnalysis = data.lastAnalysisResult();
            this.todayAnalysis = data.todayAnalysisResult();
            this.thisWeekAnalysis = data.thisWeekAnalysisResult();
            this.thisMonthAnalysis = data.thisMonthAnalysisResult();
            this.allTimeAnalysis = data.allTimeAnalysisResult();

            //計算結果をチャートやテーブルに反映
            setupChoiceBox();
            setupLineCharts(data.filteredResult().recent10Results());

            setupStatsTableColumn(wpmPeriodColumn, wpmDataColumn);
            setupStatsTableColumn(accuracyPeriodColumn, accuracyDataColumn);
            setupStatsTables(data.statistics());

            //誤答リストタブのテーブルにデータを設定
            populateMistakesTable(lastMistakesTable, data.filteredResult().lastResult() != null ? List.of(data.filteredResult().lastResult()) : Collections.emptyList());
            populateMistakesTable(todayMistakesTable, data.filteredResult().todayResults());
            populateMistakesTable(thisWeekMistakesTable, data.filteredResult().thisWeekResults());
            populateMistakesTable(thisMonthMistakesTable, data.filteredResult().thisMonthResults());
            populateMistakesTable(allTimeMistakesTable, data.filteredResult().allResults());

            mistakeListAccordion.setExpandedPane(lastMistakeListPane);

            //ロード中表示を解除
            loadingIndicator.setVisible(false);
            loadingOverlay.setMouseTransparent(true);
        });
        
        //Taskが異常終了した場合の処理
        loadDataTask.setOnFailed(event -> {
            loadDataTask.getException().printStackTrace();
            //ロード中表示を解除してUIをフリーズさせない
            loadingIndicator.setVisible(false);
            loadingOverlay.setMouseTransparent(true);
        });

        //Taskを別スレッドで開始する
        new Thread(loadDataTask).start();
    }

    private void updateMistakeStatesDisplay(Period period) {
        Map<MistakeType, Integer> selectedMistakeStats;
        WeaknessAnalysisResult selectedAnalysisResult;
        switch (period) {
            case LAST:
                selectedMistakeStats = this.lastMistakeStats;
                selectedAnalysisResult = this.lastAnalysis;
                break;
            case TODAY:
                selectedMistakeStats = this.todayMistakeStats;
                selectedAnalysisResult = this.todayAnalysis;
                break;
            case THISWEEK:
                selectedMistakeStats = this.thisWeekMistakeStats;
                selectedAnalysisResult = this.thisWeekAnalysis;
                break;
            case THISMONTH:
                selectedMistakeStats = this.thisMonthMistakeStats;
                selectedAnalysisResult = thisMonthAnalysis;
                break;        
            default:
                selectedMistakeStats = this.allTimeMistakeStats;
                selectedAnalysisResult = this.allTimeAnalysis;
                break;
        }
        ObservableList<MistakeStatsRow> mistakestats = FXCollections.observableArrayList();
        for (XYChart.Data<String, Integer> data : mistakeSeries.getData()) {
            MistakeType type = MistakeType.fromjapaneseName(data.getXValue());
            if (type != null) {
                data.setYValue((selectedMistakeStats.getOrDefault(type, 0)));
            }
        }
        for (Map.Entry<MistakeType, Integer> entry : selectedMistakeStats.entrySet()) {
            mistakestats.add(new MistakeStatsRow(entry.getKey().getJapaneseName(), entry.getValue()));
        }
        barChart.setTitle(period + "のミス内訳");
        this.mistakeStatsTable.setItems(mistakestats);

        displayWeaknessAnalysis(selectedAnalysisResult);
    }

    private Map<MistakeType, Integer> calculateMistakeStats(List<TestResult> results) {
        Map<MistakeType, Integer> stats = new EnumMap<>(MistakeType.class);
        for (MistakeType type : MistakeType.values()) {
            stats.put(type, 0);
        }

        if (results != null) {
            for (TestResult result : results) {
                if (result == null) continue;

                for (WordResult wordResult : result.results().values()) {
                    if (!wordResult.isCorrect()) {
                        for (MistakeDetail mistakes : wordResult.mistakes()) {
                            MistakeType type = mistakes.mistakeType();
                            stats.put(type, stats.get(type) + 1);
                        }
                    }
                }
            }
        }
        return stats;
    }

    private void setupBarChart() {
        for (MistakeType type : MistakeType.values()) {
            mistakeSeries.getData().add(new XYChart.Data<>(type.getJapaneseName(), 0));
        }
        barChart.getData().add(mistakeSeries);
    }

    private void setupMistakeStatsTable(
        TableColumn<MistakeStatsRow, String> mistakeTypeColumn,
        TableColumn<MistakeStatsRow, Integer> mistakeCountColumn
    ) {
        mistakeTypeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().mistakeType()));
        mistakeCountColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().count()).asObject());
    }

    private void setupChoiceBox() {
        periodChoiceBox.getItems().addAll(Period.values());
        periodChoiceBox.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    updateMistakeStatesDisplay(newValue);
                }
            }
        );

        periodChoiceBox.getSelectionModel().select(Period.LAST);
    }

    private void displayWeaknessAnalysis(WeaknessAnalysisResult analysisResult) {
        populateWeaknessTable(substitutionTableView,
        analysisResult.topSubstitutions(),
        entry -> new WeaknessPairRow(String.format("%c -> %c", entry.getKey().expected(), entry.getKey().actual()), entry.getValue()));

        populateWeaknessTable(transpositionTableView,
        analysisResult.topTranspositions(),
        entry -> new WeaknessPairRow(String.format("%c と %c", entry.getKey().char1(), entry.getKey().char2()), entry.getValue()));

        populateWeaknessTable(deletionTableView,
        analysisResult.topDeletions(),
        entry -> new WeaknessCharRow(entry.getKey(), entry.getValue()));

        populateWeaknessTable(insertionTableView,
        analysisResult.topInsertions(),
        entry -> {
            char beforeChar = entry.getKey().beforeChar();
            char afterChar = entry.getKey().afterChar();
            char insertionChar = entry.getKey().insertionChar();

            String beforeStr = (beforeChar == '\0') ? "(S)" : String.valueOf(beforeChar);
            String afterStr = (afterChar == '\0') ? "(E)" : String.valueOf(afterChar);
            String insertionStr = String.valueOf(insertionChar);

            return new WeaknessPairRow(String.format("%s -> [%s] -> %s", beforeStr, insertionStr, afterStr), entry.getValue());
        });
    }

    //T:..Pair or character K:..Row
    private <T,K> void populateWeaknessTable(TableView<K> tableView, Map<T, Long> data, Function<Map.Entry<T, Long>, K> mapper) {
        ObservableList<K> tableData = data.entrySet().stream()
            .map(mapper)
            .collect(Collectors.toCollection(FXCollections::observableArrayList));

        tableView.setItems(tableData);
        tableView.setPlaceholder(new Label("まだミスがありません"));
    }

    private void setupWeaknessPairTableColumn(TableColumn<WeaknessPairRow, String> pairColumn, TableColumn<WeaknessPairRow, Long> countColumn) {
        pairColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().mistakePair()));
        pairColumn.setCellFactory(param -> new TableCell<WeaknessPairRow, String>() {
            private final Text text = new Text();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    text.setText(item);
                    // カラムの幅に合わせて折り返す（少し余裕を持たせるために -10 しています）
                    text.wrappingWidthProperty().bind(getTableColumn().widthProperty().subtract(10));
                    setGraphic(text);
                }
            }
        });
        countColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().count()));
    }

    private void setupWeaknessCharacterTableColumn(TableColumn<WeaknessCharRow, Character> characterColumn, TableColumn<WeaknessCharRow, Long> countColumn) {
        characterColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().character()));
        countColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().count()));
    }

    private void setupStatsTables(DisplayStatistics statistics) {
        ObservableList<StatsTableRow> wpmData = FXCollections.observableArrayList(
            new StatsTableRow("前回", statistics.last().averageWpm()),
            new StatsTableRow("今日", statistics.today().averageWpm()),
            new StatsTableRow("今週", statistics.thisWeek().averageWpm()),
            new StatsTableRow("今月", statistics.thisMonth().averageWpm()),
            new StatsTableRow("全期間", statistics.allTime().averageWpm())
        );

        ObservableList<StatsTableRow> accuracyData = FXCollections.observableArrayList(
            new StatsTableRow("前回", statistics.last().averageAccuracy()),
            new StatsTableRow("今日", statistics.today().averageAccuracy()),
            new StatsTableRow("今週", statistics.thisWeek().averageAccuracy()),
            new StatsTableRow("今月", statistics.thisMonth().averageAccuracy()),
            new StatsTableRow("全期間", statistics.allTime().averageAccuracy())
        );

        wpmStatsTable.setItems(wpmData);
        accuracyTable.setItems(accuracyData);
    }

    private void setupLineCharts(List<TestResult> recent10Results) {
        wpmSeries = new XYChart.Series<>();
        wpmSeries.setName("WPM推移");

        accuracySeries = new XYChart.Series<>();
        accuracySeries.setName("正答率推移");

        for (int i = 0; i < recent10Results.size(); i++) {
            TestResult result = recent10Results.get(i);
            String xAxisLable = (recent10Results.size() - i) + "回前";

            wpmSeries.getData().add(new XYChart.Data<>(xAxisLable, result.wpm()));
            accuracySeries.getData().add(new XYChart.Data<>(xAxisLable, result.accuracy()));
        }
        wpmLineChart.getData().add(wpmSeries);
        accuracyLineChart.getData().add(accuracySeries);
    }

    private void setupStatsTableColumn(
        TableColumn<StatsTableRow, String> periodColumn,
        TableColumn<StatsTableRow, Number> dataColumn
    ) {
        periodColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().period()));
        dataColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().stats()));
    }

    //以下誤答リスト用
    private void populateMistakesTable(TableView<MistakeDetailRow> tableView, List<TestResult> results) {
        ObservableList<MistakeDetailRow> mistakeData = FXCollections.observableArrayList();

        if (results != null) {
            for (TestResult result : results) {
                if (result == null) continue;

                LinkedHashMap<String, WordResult> wordResults = result.results();
                for (String question : wordResults.keySet()) {
                    WordResult wordResult = wordResults.get(question);
                    if (!wordResult.isCorrect()) {
                        List<MistakeType> mistakeTypes = wordResult.mistakes().stream().map(mistake -> mistake.mistakeType()).toList();
                        int subCount = Collections.frequency(mistakeTypes, MistakeType.SUBSTITUTION);
                        int transCount = Collections.frequency(mistakeTypes, MistakeType.TRANSPOSITION);
                        int delCount = Collections.frequency(mistakeTypes, MistakeType.DELETION);
                        int insCount = Collections.frequency(mistakeTypes, MistakeType.INSERTION);

                        mistakeData.add(new MistakeDetailRow(question, wordResult.answer(), subCount, transCount, delCount, insCount));
                    }
                }
            }
        }

        tableView.setItems(mistakeData);
        tableView.setPlaceholder(new Label("まだミスはありません"));
    }

    private void setupDetailTableColumns(
        TableColumn<MistakeDetailRow, String> questionColumn,
        TableColumn<MistakeDetailRow, String> answerColumn,
        TableColumn<MistakeDetailRow, Integer> subColumn,
        TableColumn<MistakeDetailRow, Integer> transColumn,
        TableColumn<MistakeDetailRow, Integer> delColumn,
        TableColumn<MistakeDetailRow, Integer> insColumn
    ) {
        questionColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().questionWord()));
        answerColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().answerWord()));
        subColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().subCount()).asObject());
        transColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().transCount()).asObject());
        delColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().delCount()).asObject());
        insColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().insCount()).asObject());
    }
}
