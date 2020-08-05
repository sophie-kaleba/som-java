/**
 * Copyright (c) 2009 Michael Haupt, michael.haupt@hpi.uni-potsdam.de
 * Software Architecture Group, Hasso Plattner Institute, Potsdam, Germany
 * http://www.hpi.uni-potsdam.de/swa/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package som.vmobjects;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

import som.primitives.Primitives;
import som.vm.Universe;


public class SClass extends SObject {

  private final Universe universe;

  public SClass(final Universe universe) {
    // Initialize this class by calling the super constructor
    super(universe.nilObject);
    invokablesTable = new HashMap<SSymbol, SInvokable>();
    this.universe = universe;
  }

  public SClass(final int numberOfFields, final Universe universe) {
    // Initialize this class by calling the super constructor with the given
    // value
    super(numberOfFields, universe.nilObject);
    invokablesTable = new HashMap<SSymbol, SInvokable>();
    this.universe = universe;
  }

  public Universe getUniverse() {
    return universe;
  }

  public SObject getSuperClass() {
    // Get the super class by reading the field with super class index
    return superclass;
  }

  public void setSuperClass(final SObject value) {
    // Set the super class by writing to the field with super class index
    superclass = value;
  }

  public boolean hasSuperClass() {
    // Check whether or not this class has a super class
    return superclass != universe.nilObject;
  }

  public SSymbol getName() {
    // Get the name of this class by reading the field with name index
    return name;
  }

  public void setName(final SSymbol value) {
    // Set the name of this class by writing to the field with name index
    name = value;
  }

  public SArray getInstanceFields() {
    // Get the instance fields by reading the field with the instance fields
    // index
    return instanceFields;
  }

  public void setInstanceFields(final SArray value) {
    // Set the instance fields by writing to the field with the instance
    // fields index
    instanceFields = value;
  }

  public SArray getInstanceInvokables() {
    // Get the instance invokables by reading the field with the instance
    // invokables index
    return instanceInvokables;
  }

  public void setInstanceInvokables(final SArray value) {
    // Set the instance invokables by writing to the field with the instance
    // invokables index
    instanceInvokables = value;

    // Make sure this class is the holder of all invokables in the array
    for (int i = 0; i < getNumberOfInstanceInvokables(); i++) {
      getInstanceInvokable(i).setHolder(this);
    }
  }

  public int getNumberOfInstanceInvokables() {
    // Return the number of instance invokables in this class
    return getInstanceInvokables().getNumberOfIndexableFields();
  }

  public SInvokable getInstanceInvokable(final int index) {
    // Get the instance invokable with the given index
    return (SInvokable) getInstanceInvokables().getIndexableField(index);
  }

  public void setInstanceInvokable(final int index, final SInvokable value) {
    // Set this class as the holder of the given invokable
    value.setHolder(this);

    // Set the instance method with the given index to the given value
    getInstanceInvokables().setIndexableField(index, (SAbstractObject) value);
  }

  @Override
  public int getDefaultNumberOfFields() {
    // Return the default number of fields in a class
    return numberOfClassFields;
  }

  @TruffleBoundary
  public SInvokable lookupInvokable(final SSymbol signature) {
    SInvokable invokable;

    // Lookup invokable and return if found
    invokable = invokablesTable.get(signature);
    if (invokable != null) {
      return invokable;
    }

    // Lookup invokable with given signature in array of instance invokables
    for (int i = 0; i < getNumberOfInstanceInvokables(); i++) {
      // Get the next invokable in the instance invokable array
      invokable = getInstanceInvokable(i);

      // Return the invokable if the signature matches
      if (invokable.getSignature() == signature) {
        invokablesTable.put(signature, invokable);
        return invokable;
      }
    }

    // Traverse the super class chain by calling lookup on the super class
    if (hasSuperClass()) {
      invokable = ((SClass) getSuperClass()).lookupInvokable(signature);
      if (invokable != null) {
        invokablesTable.put(signature, invokable);
        return invokable;
      }
    }

    // Invokable not found
    return null;
  }

  public int lookupFieldIndex(final SSymbol fieldName) {
    // Lookup field with given name in array of instance fields
    for (int i = getNumberOfInstanceFields() - 1; i >= 0; i--) {
      // Return the current index if the name matches
      if (fieldName == getInstanceFieldName(i)) {
        return i;
      }
    }

    // Field not found
    return -1;
  }

  public boolean addInstanceInvokable(final SInvokable value) {
    // Add the given invokable to the array of instance invokables
    for (int i = 0; i < getNumberOfInstanceInvokables(); i++) {
      // Get the next invokable in the instance invokable array
      SInvokable invokable = getInstanceInvokable(i);

      // Replace the invokable with the given one if the signature matches
      if (invokable.getSignature() == value.getSignature()) {
        setInstanceInvokable(i, value);
        return false;
      }
    }

    // Append the given method to the array of instance methods
    setInstanceInvokables(getInstanceInvokables().copyAndExtendWith(
        (SAbstractObject) value, universe));
    return true;
  }

  public void addInstancePrimitive(final SPrimitive value) {
    addInstancePrimitive(value, false);
  }

  public void addInstancePrimitive(final SPrimitive value, final boolean suppressWarning) {
    if (addInstanceInvokable(value) && !suppressWarning) {
      Universe.print("Warning: Primitive " + value.getSignature().getEmbeddedString());
      Universe.println(" is not in class definition for class "
          + getName().getEmbeddedString());
    }
  }

  public SSymbol getInstanceFieldName(int index) {
    // Get the name of the instance field with the given index
    if (index >= getNumberOfSuperInstanceFields()) {
      // Adjust the index to account for fields defined in the super class
      index -= getNumberOfSuperInstanceFields();

      // Return the symbol representing the instance fields name
      return (SSymbol) getInstanceFields().getIndexableField(index);
    } else {
      // Ask the super class to return the name of the instance field
      return ((SClass) getSuperClass()).getInstanceFieldName(index);
    }
  }

  public int getNumberOfInstanceFields() {
    // Get the total number of instance fields in this class
    return getInstanceFields().getNumberOfIndexableFields()
        + getNumberOfSuperInstanceFields();
  }

  private int getNumberOfSuperInstanceFields() {
    // Get the total number of instance fields defined in super classes
    if (hasSuperClass()) {
      return ((SClass) getSuperClass()).getNumberOfInstanceFields();
    } else {
      return 0;
    }
  }

  public void setInstanceFields(final String[] fields) {
    // Allocate an array of the right size
    SArray instanceFields = universe.newArray(fields.length);

    // Iterate through all the given fields
    for (int i = 0; i < fields.length; i++) {
      // Insert the symbol corresponding to the given field string in the
      // array
      instanceFields.setIndexableField(i, universe.symbolFor(fields[i]));
    }

    // Set the instance fields of this class to the new array
    setInstanceFields(instanceFields);
  }

  public boolean hasPrimitives() {
    // Lookup invokable with given signature in array of instance invokables
    for (int i = 0; i < getNumberOfInstanceInvokables(); i++) {
      // Get the next invokable in the instance invokable array
      if (getInstanceInvokable(i).isPrimitive()) {
        return true;
      }
    }
    return false;
  }

  public void loadPrimitives() {
    // Compute the class name of the Java(TM) class containing the
    // primitives
    String className = "som.primitives." + getName().getEmbeddedString()
        + "Primitives";

    // Try loading the primitives
    try {
      Class<?> primitivesClass = Class.forName(className);
      try {
        Constructor<?> ctor = primitivesClass.getConstructor(Universe.class);
        ((Primitives) ctor.newInstance(universe)).installPrimitivesIn(this);
      } catch (Exception e) {
        Universe.println("Primitives class " + className
            + " cannot be instantiated");
      }
    } catch (ClassNotFoundException e) {
      Universe.println("Primitives class " + className + " not found");
    }
  }

  @Override
  public String toString() {
    return "Class(" + getName().getEmbeddedString() + ")";
  }

  // Implementation specific fields
  private SObject superclass;
  private SSymbol name;
  private SArray  instanceInvokables;
  private SArray  instanceFields;

  // Mapping of symbols to invokables
  private final HashMap<SSymbol, SInvokable> invokablesTable;

  // Static field indices and number of class fields
  static final int numberOfClassFields = numberOfObjectFields;

}
