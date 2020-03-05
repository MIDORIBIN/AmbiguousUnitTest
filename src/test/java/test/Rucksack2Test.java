package test;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.List;
import static org.junit.Assert.*;

public class Rucksack2Test {
    @Rule
    public Timeout globalTimeout = Timeout.millis(1000);

    @Test
    public void add() throws NoSuchFieldException, IllegalAccessException {
        Rucksack2 rucksack = new Rucksack2();
        Gum gum1 = new Gum(100);
        Gum gum2 = new Gum(200);
        rucksack.addd(gum1);
        rucksack.addd(gum2);

        Field arrayListField = Rucksack2.class.getDeclaredField("arraylist");
        arrayListField.setAccessible(true);
        List<Gum> arrayList = autoCast(arrayListField.get(rucksack));
        assertEquals(gum1, arrayList.get(0));
        assertEquals(gum2, arrayList.get(1));
    }

    @Test
    public void getSum() throws NoSuchFieldException, IllegalAccessException {
        Rucksack2 rucksack = new Rucksack2();
        Field arrayListField = Rucksack2.class.getDeclaredField("arraylist");
        arrayListField.setAccessible(true);
        List<Gum> arrayList = autoCast(arrayListField.get(rucksack));

        assertEquals(0, rucksack.setSum());

        arrayList.add(new Gum(100));
        assertEquals(100, rucksack.setSum());

        arrayList.add(new Gum(200));
        assertEquals(300, rucksack.setSum());
    }

    @Test
    public void printItems() throws NoSuchFieldException, IllegalAccessException {
        Rucksack2 rucksack = new Rucksack2();
        Field arrayListField = Rucksack2.class.getDeclaredField("arraylist");
        arrayListField.setAccessible(true);
        List<Gum> arrayList = autoCast(arrayListField.get(rucksack));

        // 標準出力を取得
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        rucksack.printItem();
        String expected1 = "";
        String actual1 = outContent.toString();
        assertEquals(expected1, actual1);

        outContent.reset();
        arrayList.add(new Gum(100));
        rucksack.printItem();
        String expected2 = "ガム 100円" + System.lineSeparator();
        String actual2 = outContent.toString();
        assertEquals(expected2, actual2);

        outContent.reset();
        arrayList.add(new Gum(200));
        rucksack.printItem();
        String expected3 = "ガム 100円" + System.lineSeparator() + "ガム 200円" + System.lineSeparator();
        String actual3 = outContent.toString();
        assertEquals(expected3, actual3);

        // 解放
        System.setOut(System.out);
    }

    @SuppressWarnings("unchecked")
    private static <T> T autoCast(Object obj) {
        return (T) obj;
    }
}