# 图片资源

当前是 600×400 的 PNG 占位图，用于先把菜品/套餐数据跑通。
后面有真实菜品图时，直接替换同名文件即可，数据库里的路径不用改。

## 菜品

| 文件 | 对应菜品 |
|---|---|
| `dish/hongshaorou.png` | 红烧肉 |
| `dish/gongbaojiding.png` | 宫保鸡丁 |
| `dish/qingchaoshishu.png` | 清炒时蔬 |
| `dish/mapodoufu.png` | 麻婆豆腐 |
| `dish/mifan.png` | 米饭 |
| `dish/miantiao.png` | 面条 |
| `dish/kele.png` | 可乐 |
| `dish/xianzhaguozhi.png` | 鲜榨果汁 |

## 套餐

| 文件 | 对应套餐 |
|---|---|
| `setmeal/danren-taocan-a.png` | 单人套餐A |
| `setmeal/danren-taocan-b.png` | 单人套餐B |
| `setmeal/shuangren-taocan.png` | 双人套餐 |

## 兜底

`placeholder.png` 是通用占位图；图片缺失时可以临时用它。

## 访问路径

数据库里存的是 `/images/...`，后续 Nginx 把 `frontend/images` 映射到 `/images/` 即可。
