"""
Provide a unified interface to a set of interfaces in a subsystem.
Facade defines a higher-level interface that makes the subsystem easier
to use.
"""
"""Author: Omprakash"""

class Facade:
    """
    Know which subsystem classes are responsible for a request.
    Delegate client requests to appropriate subsystem objects.
    """

    def __init__(self):
        print("Initialize Facade")
        self._subsystem_1 = Subsystem1()
        self._subsystem_2 = Subsystem2()

    def operation(self):
        print("Start Operations")
        self._subsystem_1.operation1()
        self._subsystem_1.operation2()
        self._subsystem_2.operation1()
        self._subsystem_2.operation2()


class Subsystem1:
    """
    Implement subsystem functionality.
    Handle work assigned by the Facade object.
    Have no knowledge of the facade; that is, they keep no references to
    it.
    """
    def __init__(self):
        print("Initialize Subsystem 1")

    def operation1(self):
        print("Sub1-Oper1")

    def operation2(self):
        print("Sub1-Oper2")


class Subsystem2:
    """
    Implement subsystem functionality.
    Handle work assigned by the Facade object.
    Have no knowledge of the facade; that is, they keep no references to
    it.
    """
    def __init__(self):
        print("Initialize Subsystem 2")

    def operation1(self):
        print("Sub2-Oper1")

    def operation2(self):
        print("Sub2-Oper2")


def main():
    facade = Facade()
    facade.operation()


if __name__ == "__main__":
    main()
