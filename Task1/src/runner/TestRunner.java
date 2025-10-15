package runner;

import runner.annotations.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class TestRunner {
    private TestRunner() {}

    public static void runTests(Class<?> testClass) {
        if (testClass == null) {
            throw new IllegalArgumentException("testClass must not be null");
        }

        Method beforeSuite = null;
        Method afterSuite = null;
        List<Method> beforeEach = new ArrayList<>();
        List<Method> afterEach = new ArrayList<>();
        List<Method> tests = new ArrayList<>();

        for (Method method : testClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(BeforeSuite.class)) {
                ensureStatic(method, "@BeforeSuite must annotate a static method");
                ensureUnique(beforeSuite, "Multiple @BeforeSuite methods are not allowed");
                beforeSuite = method;
            }
            if (method.isAnnotationPresent(AfterSuite.class)) {
                ensureStatic(method, "@AfterSuite must annotate a static method");
                ensureUnique(afterSuite, "Multiple @AfterSuite methods are not allowed");
                afterSuite = method;
            }
            if (method.isAnnotationPresent(BeforeTest.class)) {
                beforeEach.add(method);
            }
            if (method.isAnnotationPresent(AfterTest.class)) {
                afterEach.add(method);
            }
            if (method.isAnnotationPresent(Test.class)) {
                tests.add(method);
            }
        }

        for (Method m : tests) {
            int p = m.getAnnotation(Test.class).priority();
            if (p < 1 || p > 10) {
                throw new IllegalStateException("@Test priority must be in [1,10] for method: " + signatureOf(m));
            }
        }

        tests.sort(Comparator
                .comparingInt((Method m) -> m.getAnnotation(Test.class).priority())
                .reversed()
                .thenComparing(Method::getName));

        Object instance = null;
        if (!tests.isEmpty() || !beforeEach.isEmpty() || !afterEach.isEmpty()) {
            instance = instantiate(testClass);
        }

        if (beforeSuite != null) {
            invoke(beforeSuite, null);
        }

        for (Method testMethod : tests) {
            for (Method before : beforeEach) {
                invoke(before, instance);
            }

            CsvSource csv = testMethod.getAnnotation(CsvSource.class);
            if (csv != null) {
                Object[] args = parseCsvArgs(csv.value(), testMethod.getParameterTypes());
                invoke(testMethod, instance, args);
            } else {
                ensureNoParams(testMethod);
                invoke(testMethod, instance);
            }

            for (Method after : afterEach) {
                invoke(after, instance);
            }
        }

        if (afterSuite != null) {
            invoke(afterSuite, null);
        }
    }

    private static void ensureStatic(Method method, String message) {
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalStateException(message + ": " + signatureOf(method));
        }
    }

    private static void ensureUnique(Method existing, String message) {
        if (existing != null) {
            throw new IllegalStateException(message);
        }
    }

    private static void ensureNoParams(Method method) {
        if (method.getParameterCount() != 0) {
            throw new IllegalStateException("Test method must have no parameters or be annotated with @CsvSource: " + signatureOf(method));
        }
    }

    private static Object instantiate(Class<?> testClass) {
        try {
            return testClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to instantiate test class: " + testClass.getName(), e);
        }
    }

    private static void invoke(Method method, Object instance, Object... args) {
        boolean access = method.canAccess(instance);
        try {
            if (!access) method.setAccessible(true);
            method.invoke(instance, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            Throwable cause = (e instanceof InvocationTargetException && e.getCause() != null) ? e.getCause() : e;
            throw new RuntimeException("Error invoking method: " + signatureOf(method) + " -> " + cause, cause);
        } finally {
            if (!access) method.setAccessible(false);
        }
    }

    private static String signatureOf(Method m) {
        return m.getDeclaringClass().getSimpleName() + "." + m.getName() + Arrays.toString(m.getParameterTypes());
    }

    private static Object[] parseCsvArgs(String csv, Class<?>[] parameterTypes) {
        String[] parts = splitCsv(csv);
        if (parts.length != parameterTypes.length) {
            throw new IllegalStateException("@CsvSource elements count (" + parts.length + ") does not match method parameters (" + parameterTypes.length + ")");
        }
        Object[] args = new Object[parts.length];
        for (int i = 0; i < parts.length; i++) {
            args[i] = convert(parts[i].trim(), parameterTypes[i]);
        }
        return args;
    }

    private static String[] splitCsv(String csv) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < csv.length(); i++) {
            char ch = csv.charAt(i);
            if (ch == '"') {
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                tokens.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        tokens.add(current.toString().trim());
        for (int i = 0; i < tokens.size(); i++) {
            String t = tokens.get(i);
            if (t.length() >= 2 && t.startsWith("\"") && t.endsWith("\"")) {
                tokens.set(i, t.substring(1, t.length() - 1));
            }
        }
        return tokens.toArray(new String[0]);
    }

    private static Object convert(String value, Class<?> targetType) {
        if (targetType == String.class) return value;
        if (targetType == int.class || targetType == Integer.class) return Integer.parseInt(value);
        if (targetType == long.class || targetType == Long.class) return Long.parseLong(value);
        if (targetType == boolean.class || targetType == Boolean.class) return Boolean.parseBoolean(value);
        if (targetType == double.class || targetType == Double.class) return Double.parseDouble(value);
        if (targetType == float.class || targetType == Float.class) return Float.parseFloat(value);
        if (targetType == short.class || targetType == Short.class) return Short.parseShort(value);
        if (targetType == byte.class || targetType == Byte.class) return Byte.parseByte(value);
        if (targetType == char.class || targetType == Character.class) {
            if (value.length() != 1) throw new IllegalArgumentException("Cannot convert to char: '" + value + "'");
            return value.charAt(0);
        }
        throw new IllegalArgumentException("Unsupported parameter type: " + targetType.getName());
    }
}


