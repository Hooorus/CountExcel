package cn.calendo.controller;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/comp_survey")
public class WebExcelHutool {

    /**
     * @param file       待计算的综测表格
     * @param index_row  序号行
     * @param index_col  序号列
     * @param score_row  评分行
     * @param score_col  评分列
     * @param name_row   姓名行
     * @param name_col   姓名列
     * @param class_num  班级人数（对应的是sheet表的总量）
     * @param self_miss  忽略自评与否 true：忽略；false：不忽略
     * @param exclude_up 得分去最大值 true：去除；false：不去除
     * @param exclude_dn 得分去最小值 true：去除；false：不去除
     * @return list数组，以索引为序
     */
    @PostMapping("/upload")
    public String upload(MultipartFile file,
                         @RequestParam(value = "index_row") int index_row,
                         @RequestParam(value = "index_col") String index_col,
                         @RequestParam(value = "score_row") int score_row,
                         @RequestParam(value = "score_col") String score_col,
                         @RequestParam(value = "name_row") int name_row,
                         @RequestParam(value = "name_col") String name_col,
                         @RequestParam(value = "class_num") int class_num,
                         @RequestParam(value = "self_miss") boolean self_miss,
                         @RequestParam(value = "exclude_up") boolean exclude_up,
                         @RequestParam(value = "exclude_dn") boolean exclude_dn) throws IOException {
        ArrayList<String> total = new ArrayList<>();
        TreeMap<Integer, String> treeMap = new TreeMap<>();
        int self_cnt = 0;
        if (self_miss){
            self_cnt = 1;
        }else {
            self_cnt = 0;
        }
        //需要排除个人最大/最小值
        if (exclude_dn || exclude_up) {
            //班级人数列
            for (int number = 0; number < class_num; number++) {
                ArrayList<Double> list = new ArrayList<>();
                Number index_value = 0;
                String name_value = "";
                //sheet表张
                for (int sheet = 0; sheet < class_num; sheet++) {
                    InputStream inputStream = file.getInputStream();
                    ExcelReader reader = ExcelUtil.getReader(inputStream, sheet);
                    //获取单张表内的number行序号值
                    index_value = (Number) reader.readCellValue(index_col.trim().toUpperCase().charAt(0) - 65, number + index_row - 1);
                    //获取单张表内的number行姓名值
                    name_value = String.valueOf(reader.readCellValue(name_col.trim().toUpperCase().charAt(0) - 65, number + name_row - 1));
                    //获取单张表内的number行评分值
                    Number score_value = (Number) reader.readCellValue(score_col.trim().toUpperCase().charAt(0) - 65, number + score_row - 1);
                    list.add(score_value.doubleValue());
                }
                list.sort(Comparator.naturalOrder());
                //去掉最大值
                int flag_up = 0;
                if (exclude_up) {
                    list.remove(0);
                    flag_up = 1;
                }
                //去掉最小值
                int flag_dn = 0;
                if (exclude_dn) {
                    list.remove(class_num - 1 - flag_up);
                    flag_dn = 1;
                }
                Double single_score = 0.0;
                //单人统计
                for (Double item : list) {
                    single_score += item;
                }
                single_score /= (class_num - flag_up - flag_dn);
                treeMap.put(index_value.intValue(), name_value + " " + single_score);
            }
            for (int i = 1; i <= treeMap.size(); i++) {
                total.add(i + ": " + treeMap.get(i));
            }
            //不需排除个人最大/最小值
        } else {
            //sheet表页
            double[] score = new double[class_num + 50];
            String[] name = new String[class_num + 50];
            for (int sheet = 0; sheet < class_num; sheet++) {
                InputStream inputStream = file.getInputStream();
                Number index_value;
                ExcelReader reader = ExcelUtil.getReader(inputStream, sheet);
                //班级人数行
                for (int number = 0; number < class_num; number++) {
                    //获取单张表内的number行序号值
                    index_value = (Number) reader.readCellValue(index_col.trim().toUpperCase().charAt(0) - 65, number + index_row - 1);
                    //获取单张表内的number行评分值
                    Number score_value = (Number) reader.readCellValue(score_col.trim().toUpperCase().charAt(0) - 65, number + score_row - 1);
                    score[index_value.intValue()] += score_value.doubleValue();
                }
            }
            InputStream inputStream = file.getInputStream();
            ExcelReader reader = ExcelUtil.getReader(inputStream);
            int head = ((Number) reader.readCellValue(index_col.trim().toUpperCase().charAt(0) - 65, index_row - 1)).intValue();
            for (int i = head; i < head + class_num; i++) {
                String name_value = String.valueOf(reader.readCellValue(name_col.trim().toUpperCase().charAt(0) - 65, i + name_row - 1 - 1));
                name[i] = name_value;
                score[i] /= (class_num - self_cnt);
                total.add(i + ": " + name[i] + " " + score[i]);
            }
        }
        String s = JSON.toJSONString(total);
        System.out.println(s);
        return s;
    }
}
