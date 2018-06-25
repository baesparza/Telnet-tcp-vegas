package aplicacion.utils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bruno
 */
public class ListPackages {

    private List<DATAPackage> packages;

    public ListPackages() {
        this.packages = new ArrayList<>();
    }

    public void add(DATAPackage pack) {
        int id = Integer.valueOf(pack.id);
        if (id == packages.size()) {
            // the package is in order, can be added to list
            packages.add(pack);
            return;
        }
        // search position backwards
        for (int i = packages.size() - 1; i <= 0; i++) {
            if (Integer.valueOf(packages.get(i).id) > id) {
                continue;
            }
            // found position
            packages.add(i + 1, pack);
            return;
        }
    }

    public void clear() {
        this.packages.clear();
    }

    public String getMessage() {
        StringBuilder resp = new StringBuilder();
        for (DATAPackage p : packages) {
            resp.append(p.data).append(" ");
        }
        return resp.toString();
    }

    public boolean hasEnded() {
        if (packages.isEmpty()) {
            // no packages
            return false;
        }
        return 0 == Integer.parseInt(this.getLastPackage().fragement.trim());
    }

    public DATAPackage getLastPackage() {
        if (packages.isEmpty()) {
            // no packages
            return null;
        }
        return packages.get(packages.size() - 1);
    }

}
