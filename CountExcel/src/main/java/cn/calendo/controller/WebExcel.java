package cn.calendo.controller;

import cn.calendo.pojo.ClassMates;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@CrossOrigin
@RestController
public class WebExcel {

    @PostMapping("/upload")
    public List<Double> upload(MultipartFile file,
                               @RequestParam(value = "num_cnt") int num_cnt,
                               @RequestParam(value = "head_row") int head_row) throws IOException {
        double[] total = new double[num_cnt + 1];
        double anno;
        ArrayList<Double> list = new ArrayList<>();
        for (int i = 0; i <= num_cnt - 1; i++) {
            EasyExcel.read(file.getInputStream(), ClassMates.class, new AnalysisEventListener<ClassMates>() {
                @Override
                public void invoke(ClassMates classMates, AnalysisContext analysisContext) {
                    String score = classMates.getScore();
                    int id = Integer.parseInt(classMates.getId());
                    total[id] += Double.parseDouble(score);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                }
            }).headRowNumber(head_row - 1).sheet(i).doRead();
        }
        for (int i = 1; i <= num_cnt; i++) {
            anno = total[i] / (num_cnt - 1);
            list.add(anno);
        }
        System.out.println(list);
        return list;
    }

}
