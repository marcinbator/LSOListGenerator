package pl.bator.lso_list_generator.service;

import javassist.NotFoundException;
import pl.bator.lso_list_generator.repository.GroupJSONRepository;
import pl.bator.lso_list_generator.util.PDFUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DateFormatSymbols;
import java.time.Month;
import java.time.Year;
import java.util.Locale;

public class PDFGenerationService {
    private final GroupJSONRepository groupJSONRepository;

    public PDFGenerationService(GroupJSONRepository groupJSONRepository) {
        this.groupJSONRepository = groupJSONRepository;
    }

    public void handleGenerateClick(String selectedMonth, int selectedYear, Path savePath) throws NotFoundException, IOException {
        int monthIndex = -1;
        String[] polishMonths = new DateFormatSymbols(new Locale("pl")).getMonths();
        for (int i = 0; i < polishMonths.length; i++) {
            if (selectedMonth.equals(polishMonths[i])) {
                monthIndex = i;
                break;
            }
        }
        if (monthIndex == -1) throw new NotFoundException("Month not found");

        Month month = Month.of(monthIndex + 1);

        for (int i = 0; i < groupJSONRepository.getGroups().size(); i++) {
            PDFUtil.generatePdf(groupJSONRepository.getGroups().get(i), month, Year.of(selectedYear), savePath);
        }
    }
}
