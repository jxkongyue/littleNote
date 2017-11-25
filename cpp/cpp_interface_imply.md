### 动态实现方式

```c++

class DynamicInterface
{
public:
    // non-virtual interface
    void fun()
    {
        do_fun();    // equivalent to "this->do_fun()"
    }

    // enable deletion of a Derived* through a Base*
    virtual ~DynamicInterface() = default;
private:
    // pure virtual implementation
    virtual void do_fun() = 0;
};

class DynamicImplementation
    : public DynamicInterface
{
private:
    virtual void do_fun() override 
    { 
        cout << "DynamicImplementation::do_fun" << endl;
    }
};
```
特点：
1. 析构函数声明为virtual，方面使用父指针析构
2. 接口api声明为public，具体实现声明为private。

### 静态多态方式

```c++

template<typename Derived>
class enable_down_cast
{
private:
    typedef enable_down_cast Base;
public:
    Derived const* self() const
    {
        // casting "down" the inheritance hierarchy
        return static_cast<Derived const*>(this);
    }

    Derived* self()
    {
        return static_cast<Derived*>(this);
    }
protected:
    // disable deletion of Derived* through Base*
    // enable deletion of Base* through Derived*
    ~enable_down_cast() = default; // C++11 only, use ~enable_down_cast() {} in C++98
};

template<typename Impl>
class StaticInterface
    :
    // enable static polymorphism
    public enable_down_cast<Impl>
{
private:
    // dependent name now in scope
    using enable_down_cast<Impl>::self;
public:
    // interface
    void fun() { self()->do_fun(); }
protected:
    // disable deletion of Derived* through Base*
    // enable deletion of Base* through Derived*
    ~StaticInterface() = default; // C++11 only, use ~IFooInterface() {} in C++98/03
};

class StaticImplementation
    :
    public StaticInterface< StaticImplementation >
{
private:
    // implementation
    friend class StaticInterface< StaticImplementation >;
    void do_fun() { /* your implementation here */ }
};
```
