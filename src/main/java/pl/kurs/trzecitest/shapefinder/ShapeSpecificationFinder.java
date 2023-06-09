package pl.kurs.trzecitest.shapefinder;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kurs.trzecitest.model.Shape;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class ShapeSpecificationFinder {

    private final EntityManagerFactory entityManagerFactory;

    public List<Shape> getShapeWithParams(Map<String, String> parameter) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        List<Shape> shapes = new ArrayList<>();

        try {
            String viewName = getViewName(parameter);
            if (viewName != null) {
                String queryStr = "SELECT * FROM " + viewName;
                String condition = getCondition(parameter);
                if (!condition.isEmpty()) {
                    queryStr += " WHERE " + condition;
                }
                Query query = entityManager.createNativeQuery(queryStr, Shape.class);
                shapes = query.getResultList();
            }
        } catch (NoResultException e) {
            e.getMessage();
        } finally {
            entityManager.close();
        }
        return shapes;
    }

    private String getCondition(Map<String, String> parameters) {
        List<String> conditions = new ArrayList<>();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String clearKey = key.replaceAll("[^a-zA-Z0-9\\s]", "");
            if (clearKey.endsWith("From")) {
                String keyWithoutSuffix = clearKey.substring(0, clearKey.length() - 4);
                conditions.add(keyWithoutSuffix + " >= " + value);
            } else if (clearKey.endsWith("To")) {
                String keyWithoutSuffix = clearKey.substring(0, clearKey.length() - 2);
                conditions.add(keyWithoutSuffix + " <= " + value);
            } else if (clearKey.contains("type")) {
                conditions.add("d" + clearKey + " = " + "'" + value + "'");
            } else {
                String queryKey = clearKey.replaceAll("([A-Z])", "_$1".toLowerCase());
                conditions.add(queryKey + " = " + "'" + value + "'");
            }
        }
        return String.join(" AND ", conditions);
    }

    private String getViewName(Map<String, String> params) {
        String viewName = "";
        if (params.containsKey("areaFrom") || params.containsKey("areaTo") || params.containsKey("area") ||
                params.containsKey("perimeterFrom") || params.containsKey("perimeterTo") || params.containsKey("perimeter")) {
            viewName = "shape_with_area_and_perimeter";
        } else {
            viewName = "all_shapes";
        }
        return viewName;
    }
}