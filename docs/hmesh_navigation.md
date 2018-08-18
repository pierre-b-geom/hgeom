## Navigation within a [HMesh](../src/hgeom/hmesh/elements/HMesh.java)

#### 1/ A [HMeshWalker](../src/hgeom/hmesh/core/HMeshWalker.java) can be used to move from element to element

Here is an example showing how to implement the navigation of a  [HMeshWalker](../src/hgeom/hmesh/core/HMeshWalker.java) from a specified vertex to another vertex 

```Java
   HMesh2D mesh = ...
   HVertex vertex = ...

   // Create a walker intended for walking inside the 2D mesh
   HMeshWalker walker = new HMeshWalker(mesh);

   // Get a operator that will be used for navigating. This operator 
   // compares the abscissa of 2 vertices and return the vertex with
   // the greatest abscissa
   BinaryOperator<HVertex> maxOperator = mesh.vertexXs().maxOperator();
   
   // Navigate from vertex to vertex, selecting among each 
   // vertex's neighbors, the one with the greatest abscissa until a 
   // final vertex is reached
   HVertex vertexWithGreatestAbscissa = walker.find(vertex, maxOperator);
   
   // Same navigation than before but return a stream over the 
   // complete path from the initial to the final vertex
   Stream<HVertex> pathToExtrema = walker.walk(vertex, maxOperator);
```

A [HMeshWalker](../src/hgeom/hmesh/core/HMeshWalker.java) can also move from face to face or from edge to edge or from any element to any other element

#### 2/ A [HMeshTreeWalker](../src/hgeom/hmesh/core/HMeshTreeWalker.java) can also be used to move from element to element

The walk with a [HMeshTreeWalker](../src/hgeom/hmesh/core/HMeshTreeWalker.java) is using a **tree-shaped path**. It starts from a mesh's element and circles around it through a **breadth-first** or a **depth-first** tree path 

```Java
   HMesh2D mesh = ...
   HVertex vertex = ...

   // Create a walker intended for walking inside the 2D mesh along a 
   // "breadth first" tree. Give the tree a specific depth
   int treeMaxDepth = 5;
   
   HMeshTreeWalker treeWalker = new HMeshTreeWalker(mesh,
				TreePathType.BREADTH_FIRST, treeMaxDepth);

   // Navigate from vertex to vertex along the tree path. 
   // Return a stream over the tree path 
   Stream<HVertex> treePath = treeWalker.walk(vertex);

   // Find the vertex having the lowest abscissa among the tree's elements
   Comparator<HVertex> comp = Comparator.comparingDouble(mesh::vertexX);
   HVertex v = treePath.sorted(comp).findFirst().orElse(...);
```
