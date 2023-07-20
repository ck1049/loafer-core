package com.loafer.core.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuVo {

    private Integer id;
    private Integer parentId;
    private String menuName;
    private String path;
    List<MenuVo> children;

    public static void main(String[] args) {
        String sql = "select a.* from menu a, menu_role b " +
                "where a.id = b.menu_id and b.role_id = ? order by a.parent_id asc";

        // List<MenuVo> list = queryList(sql);
        List<MenuVo> list = new ArrayList<>();
        Map<Integer, List<MenuVo>> parentMap = list.stream().collect(Collectors.groupingBy(MenuVo::getParentId, LinkedHashMap::new, Collectors.toList()));

        // 顶级菜单id0
        List<MenuVo> menuList = parentMap.get(0);
        addChildren(menuList, parentMap);
    }

    public static void addChildren(List<MenuVo> menuList, Map<Integer, List<MenuVo>> parentMap) {
        if (CollectionUtils.isEmpty(menuList)) {
            return;
        }
        for (MenuVo menuVo : menuList) {
            // menuVo的子菜单
            List<MenuVo> menuVos = parentMap.get(menuVo.getId());
            menuVo.setChildren(menuVos);
            addChildren(menuVos, parentMap);
        }
    }
}
