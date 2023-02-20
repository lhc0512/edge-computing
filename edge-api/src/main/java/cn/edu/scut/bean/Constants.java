package cn.edu.scut.bean;


public enum Constants {

    Byte(8), Kilo(1000), Mega(1000 * 1000), Giga(1000 * 1000 * 1000);

    public Integer value;

    Constants(Integer value) {
        this.value = value;
    }
}
