abstract class Shape {
    public abstract double area();
}

class Circle extends Shape {
    private final double radius;

    public Circle(double radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("Radius cannot be negative.");
        }
        this.radius = radius;
    }

    @Override
    public double area() {
        return Math.PI * radius * radius;
    }
}

class Rectangle extends Shape {
    private final double width;
    private final double height;

    public Rectangle(double width, double height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Dimensions cannot be negative.");
        }
        this.width = width;
        this.height = height;
    }

    @Override
    public double area() {
        return width * height;
    }
}

class Triangle extends Shape {
    private final double base;
    private final double height;

    public Triangle(double base, double height) {
        if (base < 0 || height < 0) {
            throw new IllegalArgumentException("Dimensions cannot be negative.");
        }
        this.base = base;
        this.height = height;
    }

    @Override
    public double area() {
        return 0.5 * base * height;
    }
}

public class ShapeAreas {
    public static void main(String[] args) {
        Shape[] shapes = {
            new Circle(3),
            new Rectangle(4, 5),
            new Triangle(6, 4),
            new Circle(2)
        };

        double totalArea = 0;
        double largestArea = -1;
        Shape largestShape = null;

        for (Shape shape : shapes) {
            double shapeArea = shape.area();
            totalArea += shapeArea;

            System.out.printf("%s area: %.2f%n",
                    shape.getClass().getSimpleName(), shapeArea);
            System.out.printf("Running total: %.2f%n", totalArea);

            if (shapeArea > largestArea) {
                largestArea = shapeArea;
                largestShape = shape;
            }
        }

        if (largestShape != null) {
            System.out.printf("Largest: %s (%.2f)%n",
                    largestShape.getClass().getSimpleName(), largestArea);
        } else {
            System.out.println("No shapes to calculate.");
        }

        System.out.printf("Total area: %.2f%n", totalArea);
    }
}