package com.tsa.database.services;



import com.tsa.database.interfaces.ResultViewer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResultViewerImpl implements ResultViewer {

    private final List<?> list;
    private static final int INDENT = 7;

    public ResultViewerImpl(List<?> list) {
        this.list = list;
    }

    @Override
    public void view() {
        List<Integer> columnWidth = new ArrayList<>();
        int positionForHead = 0;
        int previousBuilderLength = 0;
        int positionInBody;
        Map<?, ?> mapForHead = (Map<?, ?>) list.get(0);
        StringBuilder stringBuilder = new StringBuilder();
        for (Object o : mapForHead.keySet()) {
            if (positionForHead == 0) stringBuilder.append("|");
            stringBuilder.append(" ".repeat(INDENT));
            stringBuilder.append(o.toString());
            stringBuilder.append(" ".repeat(INDENT));
            stringBuilder.append("|");
            columnWidth.add(stringBuilder.length() - previousBuilderLength - 1);
            previousBuilderLength = stringBuilder.length();
            positionForHead++;
        }
        stringBuilder.append("\r\n");
        stringBuilder.append("-".repeat(stringBuilder.length() - 2));
        System.out.println(stringBuilder);

        stringBuilder.delete(0, stringBuilder.length());

        for (Object o : list) {
            int remainder;
            int currentIndent;
            positionForHead = 0;
            positionInBody = 0;
            Map<?, ?> mapBody = (Map<?, ?>) o;
            for (Object o1 : mapBody.keySet()) {
                String value = mapBody.get(o1).toString();
                currentIndent = (columnWidth.get(positionInBody) - value.length()) / 2;
                remainder = (columnWidth.get(positionInBody) - value.length()) - currentIndent;
                if (positionForHead == 0) stringBuilder.append("|");
                stringBuilder.append(" ".repeat(currentIndent));
                stringBuilder.append(mapBody.get(o1).toString());
                stringBuilder.append(" ".repeat(remainder));
                stringBuilder.append("|");
                positionForHead++;
                positionInBody++;
            }
            stringBuilder.append("\r\n");
            stringBuilder.append("-".repeat(stringBuilder.length() - 2));
            System.out.println(stringBuilder);
            stringBuilder.delete(0, stringBuilder.length());
        }
    }
}
