import java.util.*;
import java.util.stream.Collectors;

public class StreamTasks {
    
    static class Employee {
        String name;
        int age;
        String position;
        
        Employee(String name, int age, String position) {
            this.name = name;
            this.age = age;
            this.position = position;
        }
    }
    
    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(5, 2, 10, 9, 4, 3, 10, 1, 13);
        List<Employee> employees = Arrays.asList(
            new Employee("Иван", 35, "Инженер"),
            new Employee("Петр", 42, "Инженер"),
            new Employee("Мария", 28, "Менеджер"),
            new Employee("Алексей", 50, "Инженер"),
            new Employee("Елена", 38, "Инженер"),
            new Employee("Дмитрий", 45, "Инженер")
        );
        List<String> words = Arrays.asList("яблоко", "банан", "апельсин", "виноград", "киви");
        String text = "hello world hello java world java hello";
        List<String> sentences = Arrays.asList(
            "один два три четыре пять",
            "шесть семь восемь девять десять",
            "одиннадцать двенадцать тринадцать четырнадцать пятнадцать"
        );
        
        System.out.println("1. 3-е наибольшее число: " + 
            numbers.stream()
                .sorted(Collections.reverseOrder())
                .skip(2)
                .findFirst()
                .orElse(-1));
        
        System.out.println("2. 3-е наибольшее уникальное число: " + 
            numbers.stream()
                .distinct()
                .sorted(Collections.reverseOrder())
                .skip(2)
                .findFirst()
                .orElse(-1));
        
        System.out.println("3. Имена 3 самых старших инженеров: " + 
            employees.stream()
                .filter(e -> "Инженер".equals(e.position))
                .sorted((e1, e2) -> Integer.compare(e2.age, e1.age))
                .limit(3)
                .map(e -> e.name)
                .collect(Collectors.toList()));
        
        System.out.println("4. Средний возраст инженеров: " + 
            employees.stream()
                .filter(e -> "Инженер".equals(e.position))
                .mapToInt(e -> e.age)
                .average()
                .orElse(0.0));
        
        System.out.println("5. Самое длинное слово: " + 
            words.stream().max(Comparator.comparing(String::length)).orElse(""));
        
        System.out.println("6. Частота слов: " + 
            Arrays.stream(text.split("\\s+"))
                .collect(Collectors.groupingBy(w -> w, Collectors.counting())));
        
        System.out.println("7. Строки по длине и алфавиту: " + 
            words.stream()
                .sorted(Comparator.comparing(String::length).thenComparing(String::compareTo))
                .collect(Collectors.toList()));
        
        System.out.println("8. Самое длинное слово в массиве: " + 
            sentences.stream()
                .flatMap(s -> Arrays.stream(s.split("\\s+")))
                .max(Comparator.comparing(String::length))
                .orElse(""));
    }
}
