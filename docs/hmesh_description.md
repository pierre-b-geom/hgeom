## Description of [HMesh](../src/hgeom/hmesh/elements/HMesh.java)

The half-edge data structure modelized by [HMesh](../src/hgeom/hmesh/elements/HMesh.java) is composed of a collection of faces, a collection of half-edges and a collection of vertices. 

A face is modelized by the Java interface [HFace](../src/hgeom/hmesh/elements/HFace.java)

An half-edge is modelized by the Java interface [HEdge](../src/hgeom/hmesh/elements/HEdge.java)

A vertex is modelized by the Java interface [HVertex](../src/hgeom/hmesh/elements/HVertex.java)

Streams on each collection are provided by [HMesh](../src/hgeom/hmesh/elements/HMesh.java):

```Java
   HMesh mesh = ...
   
   // Get streams over the collections of faces, half-edges and vertices
   Stream<HFace> faces = mesh.faces();
   Stream<HEdge> edges = mesh.edges();
   Stream<HFace> vertices = mesh.vertices();
```

### HFace

A [HFace](../src/hgeom/hmesh/elements/HFace.java) consists of a cycle of [HEdge](../src/hgeom/hmesh/elements/HEdge.java) forming a polygon boundary. Each [HEdge](../src/hgeom/hmesh/elements/HEdge.java) connects two [HVertex](../src/hgeom/hmesh/elements/HVertex.java)

<p align="center">
	<img src="face_description.png">
</p>

[HFace](../src/hgeom/hmesh/elements/HFace.java) provides getters on its [HEdge](../src/hgeom/hmesh/elements/HEdge.java) and on its [HVertex](../src/hgeom/hmesh/elements/HVertex.java):

```Java
   HFace face = ...
   
   // Get the sequence of half-edges forming the face / polygon's boundary
   Sequence<HEdge> edges = face.edges();
   
   // Get the sequence of vertices on the face / polygon's boundary
   Sequence<HVertice> vertices = face.vertices();
```
[HFace](../src/hgeom/hmesh/elements/HFace.java) also provides a getter on its adjacent neighbors: 

```Java
   HFace face = ...
   
   // Get the sequence of adjacent faces
   Sequence<HFace> neighbors = face.neighbors();
```

### HEdge

An [HEdge](../src/hgeom/hmesh/elements/HEdge.java) modelizes an oriented link between 2 [HVertex](../src/hgeom/hmesh/elements/HVertex.java). It is an element of a cycle forming the boundary of a polygon. It always has a twin / opposite half-edge

Various getters are provided by [HEdge](../src/hgeom/hmesh/elements/HEdge.java):

```Java
   HEdge edge = ...

   // Get the face to which the half-edge belongs
   HFace face = edge.face();

   // Get the vertex the edge points at
   HVertex head = edge.head();

   // Get the vertex the edge starts from
   HVertex tail = edge.tail();
   
   // Get the next edge on the edge's cycle
   HEdge next = edge.next();

   // Get the previous edge on the edge's cycle
   HEdge previous = edge.previous();

   // Get the opposite / twin half-edge
   HEdge opposite = edge.opposite();

   // Get the cycle the edge is part of
   Sequence<HEdge> cycle = edge.cycle();
   
   // Get all edges going out of the edge's head
   Sequence<HEdge> outgoingEdges = edge.outgoingEdges()
   
   // Get all edges starting from the edge's tail
   Sequence<HEdge> incomingEdges = edge.incomingEdges()
```
### HVertex

An [HVertex](../src/hgeom/hmesh/elements/HVertex.java) modelizes a vertex. A [HVertex](../src/hgeom/hmesh/elements/HVertex.java) is both the tail of several [HEdge](../src/hgeom/hmesh/elements/HEdge.java) and the head of several others [HEdge](../src/hgeom/hmesh/elements/HEdge.java)

```Java
   HVertex vertex = ...

   // Get one of the half-edge pointing at the vertex
   HEdge edge = vertex.edge();

   // Get all half-edges going out of the vertex
   Sequence<HEdge> outgoingEdges = vertex.outgoingEdges()
   
   // Get all half-edges pointing at the vertex
   Sequence<HEdge> incomingEdges = vertex.incomingEdges()
   
   // Get all vertices neighbors of the vertex (in connection via an half-edge)
   Sequence<HVertex> neighbors = vertex.neighbors();
```

### Sequence

Some of the getters of HFace, HEdge and HVertex are returning a [Sequence](../src/hgeom/hmesh/sequence/Sequence.java). A [Sequence](../src/hgeom/hmesh/sequence/Sequence.java) is a kind of simplified stream providing several basic streaming operations:

```Java
   HVertex vertex = ...
   HEdge edge = ...
   
   // Collect all vertex neighbors whose degree is 5 into a list
   List<HVertex> neighbors = vertex.neighbors().
       filter(v -> v.degree() == 5).toList();
   
   // Iterate on the vertices of a cycle
   for (HVertex vertex : edge.cycle().map(HEdge::head)) {
      ...
   }
   
```


### Topological operations on [HMesh](../src/hgeom/hmesh/elements/HMesh.java)

Several topological operations are provided by [HMesh](../src/hgeom/hmesh/elements/HMesh.java):

 - **Splitting a face into two parts**:

```Java
   HMesh mesh = ...
   HFace face = ...
   HVertex vertex1 = ...
   HVertex vertex2 = ...
   
   // Split a face along the line formed by 2 of its vertices
   Optional<HFace> newFace = mesh.splitFace(face, vertex1, vertex2);
```

 - **Splitting a half-edge by inserting a new vertex**:

```Java
   HMesh mesh = ...
   HEdge edge = ...
   
   // Split a edge and return the newly vertex
   HVertex vertex = mesh.splitEdge(edge);
```

 - **Removing a vertex**:

```Java
   HMesh mesh = ...
   HVertex vertex = ...
   
   // Remove a vertex
   boolean success = mesh.removeVertex(vertex);
```

 - **Merging two faces**:

```Java
   HMesh mesh = ...
   HFace face1 = ...
   HFace face2 = ...
   
   // Merge 2 adjacent faces
   boolean success = mesh.mergeFaces(face1, face2);
```

### Data association with a [HMesh](../src/hgeom/hmesh/elements/HMesh.java)

Faces, half-edges and vertices of a [HMesh](../src/hgeom/hmesh/elements/HMesh.java) can be associated with data. HMesh provides several methods to do that

For instance, to associate a double value to each of the vertices of a [HMesh](../src/hgeom/hmesh/elements/HMesh.java):

```Java
   HMesh mesh = ...
   
   // Create a collection of double values associated with the mesh's vertices
   HDData<HVertex> vertexDoubleValues = mesh.createVertexDoubleData();
   
   // Associate a value to a vertex
   HVertex vertex = ...
   vertexDoubleValues.set(vertex, 2.3);
   
   // Get the value associated to a vertex
   double value = vertexDoubleValues.get(vertex);
   
   // set all double values associated with the mesh's vertices using a function
   // to compute the values
   vertexDoubleValues.setAll(v -> {
      ...
   });
```

Or to associate boolean values to the collection of edges:

```Java
   // Create a set of boolean values associated with the mesh's half-edges
   HBData<HEdge> edgeFlags = mesh.createEdgeBooleanData();
```

Or to associate Java objects to the collection of faces:

```Java
   // Create a set of data associated with the mesh's faces
   HData<HFace, Color> faceColors = mesh.createFaceData();
```

These sets of data ([HData](../src/hgeom/hmesh/data/HData.java) for Java objects, [HDData](../src/hgeom/hmesh/data/HDData.java) for double values, [HIData](../src/hgeom/hmesh/data/HIData.java) for integer values, [HBData](../src/hgeom/hmesh/data/HBData.java) for boolean values) are synchronized with their associated mesh and are updated whenever a topological change occurs. For instance, if a vertex is removed from the mesh, all values attached to this vertex will  automatically be removed from all sets of data 

### Consistency of [HMesh](../src/hgeom/hmesh/elements/HMesh.java)
An [HMesh](../src/hgeom/hmesh/elements/HMesh.java) is always consistent during its lifetime whatever operations performed on it: 

 - [HMesh](../src/hgeom/hmesh/elements/HMesh.java) never contains orphan [HVertex](../src/hgeom/hmesh/elements/HVertex.java) or [HEdge](../src/hgeom/hmesh/elements/HEdge.java): an [HEdge](../src/hgeom/hmesh/elements/HEdge.java) is always connecting 2 [HVertex](../src/hgeom/hmesh/elements/HVertex.java). A [HVertex](../src/hgeom/hmesh/elements/HVertex.java) is always the origin and the end of several [HEdge](../src/hgeom/hmesh/elements/HEdge.java) 
 - An [HEdge](../src/hgeom/hmesh/elements/HEdge.java) is always associated with an opposite/twin [HEdge](../src/hgeom/hmesh/elements/HEdge.java). The relationship is always symmetric : edge.opposite().opposite() == edge
 - An [HEdge](../src/hgeom/hmesh/elements/HEdge.java) is always part of one and only one polygonal cycle 
 - All [HEdge](../src/hgeom/hmesh/elements/HEdge.java) of a polygonal cycle are always distinct
