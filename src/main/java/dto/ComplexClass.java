package dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ComplexClass {
    private String nombre;
    private int edad;
    private List<Float> numbers;
    private List<List<String>> listOfLists;

    public static ComplexClass buildTestInstance() {
        ComplexClass complexClass = new ComplexClass();
        complexClass.setNombre("Sebastian Flores");
        complexClass.setEdad(23);

        ArrayList<Float> numbers = new ArrayList<>();
        numbers.add(new Float(12));
        numbers.add(new Float(57));
        complexClass.setNumbers(numbers);

        ArrayList<List<String>> listOfLists = new ArrayList<>();
        ArrayList<String> list1 = new ArrayList<>();
        ArrayList<String> list2 = new ArrayList<>();
        list1.add("Hola");
        list2.add("Mundo");
        list2.add("!!!");
        listOfLists.add(list1);
        listOfLists.add(list2);
        complexClass.setListOfLists(listOfLists);

        return complexClass;
    }
}
