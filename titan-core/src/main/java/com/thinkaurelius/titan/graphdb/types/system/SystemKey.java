package com.thinkaurelius.titan.graphdb.types.system;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.graphdb.internal.ElementCategory;
import com.thinkaurelius.titan.graphdb.internal.RelationCategory;
import com.thinkaurelius.titan.graphdb.internal.TitanTypeCategory;
import com.thinkaurelius.titan.graphdb.types.IndexField;
import com.thinkaurelius.titan.graphdb.types.IndexType;
import com.thinkaurelius.titan.graphdb.types.InternalIndexType;
import com.thinkaurelius.titan.graphdb.types.TypeDefinitionDescription;

import java.util.Collections;

public class SystemKey extends SystemRelationType implements TitanKey {

    private enum Index { NONE, STANDARD, UNIQUE }

    public static final SystemKey TypeName =
            new SystemKey("TypeName", String.class, 1, Index.UNIQUE, Cardinality.SINGLE);

    public static final SystemKey TypeDefinitionProperty =
            new SystemKey("TypeDefinitionProperty", Object.class, 2, Index.NONE, Cardinality.LIST);

    public static final SystemKey TypeCategory =
            new SystemKey("TypeCategory", TitanTypeCategory.class, 3, Index.STANDARD, Cardinality.SINGLE);

    public static final SystemKey TypeDefinitionDesc =
            new SystemKey("TypeDefinitionDescription", TypeDefinitionDescription.class, 4, Index.NONE, Cardinality.SINGLE);

    public static final SystemKey VertexExists =
            new SystemKey("VertexExists", Boolean.class, 7, Index.NONE, Cardinality.SINGLE);

    private final Class<?> dataType;
    private final Index index;
    private final Cardinality cardinality;

    private SystemKey(String name, Class<?> dataType, int id, Index index, Cardinality cardinality) {
        super(name, id, RelationCategory.PROPERTY);
        Preconditions.checkArgument(index!=null && cardinality!=null);
        this.dataType = dataType;
        this.index = index;
        this.cardinality = cardinality;
    }

    @Override
    public Class<?> getDataType() {
        return dataType;
    }

    @Override
    public final boolean isPropertyKey() {
        return true;
    }

    @Override
    public final boolean isEdgeLabel() {
        return false;
    }

    @Override
    public Multiplicity getMultiplicity() {
        return Multiplicity.convert(getCardinality());
    }

    @Override
    public Cardinality getCardinality() {
        return cardinality;
    }

    @Override
    public Iterable<IndexType> getKeyIndexes() {
        if (index==Index.NONE) return Collections.EMPTY_LIST;
        return ImmutableList.of((IndexType)indexDef);
    }

    private final InternalIndexType indexDef = new InternalIndexType() {

        private final IndexField[] fields = {IndexField.of(SystemKey.this)};
//        private final Set<TitanKey> fieldSet = ImmutableSet.of((TitanKey)SystemKey.this);

        @Override
        public long getID() {
            return SystemKey.this.getID();
        }

        @Override
        public IndexField[] getFields() {
            return fields;
        }

        @Override
        public IndexField getField(TitanKey key) {
            if (key.equals(SystemKey.this)) return fields[0];
            else return null;
        }

        @Override
        public boolean indexesKey(TitanKey key) {
            return getField(key)!=null;
        }

        @Override
        public Cardinality getCardinality() {
            switch(index) {
                case UNIQUE: return Cardinality.SINGLE;
                case STANDARD: return Cardinality.SET;
                default: throw new AssertionError();
            }
        }

        @Override
        public ConsistencyModifier getConsistencyModifier() {
            return ConsistencyModifier.LOCK;
        }

        @Override
        public ElementCategory getElement() {
            return ElementCategory.VERTEX;
        }

        @Override
        public boolean isInternalIndex() {
            return true;
        }

        @Override
        public boolean isExternalIndex() {
            return false;
        }

        //Use default hashcode and equals
    };


}
