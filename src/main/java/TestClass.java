import lombok.Data;

import java.util.List;

@Data
public class TestClass {
    private String nombre;
    private int edad;
    private List<Float> numbers;
    private List<List<String>> listOfLists;
}
