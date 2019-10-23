一、悬浮动态阴影效果实现：
-------------------------
+ 实现方式一：通过添加css :hover选择器实现。此方法会导致页面**重绘**，缺点**消耗过大**
- 实现方式二：通过伪类元素并修改伪元素的透明度来实现盒子阴影，此方法**不会触发重绘**

##### 下面是伪类实现的代码：
``` CSS
 .shadow_css3
{
  position:relative;
  width:200px;
  height:100px;
  border:1px solid #ccc;
}

.shadow_css3:before
{
 content:"";
 position:absolute;
 top:0;
 left:0;
 right:0;
 bottom:0;
 box-shadow: 0 0 10px 0 rgba(0, 0, 0, 0.3);
    opacity: 0;
}

.shadow_css3:hover:before
{
   opacity: 1;
}

