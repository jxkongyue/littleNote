### 模板参数传入函数指针
```c++

template<typename VType>
void test(VType src)
{
    cout << src << endl;
}

template<typename T>
void test2(T t)
{
    t(12);
}

int main()
{
    test2(test<int>);
    return 0;
}

```


### 使用宏和模板，达到反射的效果：
```c++

template<typename T>
void test2(T t)
{
    t(12);
}

#define DOMI_TYPE_SWITCH(type, DType, ...)       \
    switch (type){                               \
    case 0:                                      \
    {                                            \
        typedef float DType;                     \
        {__VA_ARGS__}                            \
    }                                            \
    break;                                       \
    case 1:                                      \
    {                                            \
        typedef int DType;                       \
        {__VA_ARGS__}                            \
    }                                            \
break;                                           \
default:                                         \
cout << "Unkonwn type enum " << type;            \
}

class Blob
{
public:
    void* data;
    int type_flag;
    Blob() : data(nullptr), type_flag(0) {};

    ~Blob()
    {
        DOMI_TYPE_SWITCH(type_flag, DType, { cout<< "delete data."<<endl; delete  Data<DType>(); });
        data = nullptr;
    }

    template<typename DType>
    inline DType*  Data()
    {
        return static_cast<DType*>(data);
    }
};

int main()
{
    Blob* b = new Blob();
    b->data = new int[4];
    b->type_flag = 1;
    delete b;
    return 0;
}
```
