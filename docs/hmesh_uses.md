## Examples of uses of [HMesh](../src/hgeom/hmesh/elements/HMesh.java)

#### 1. A function creating a [HMesh](../src/hgeom/hmesh/elements/HMesh.java) from a list of polygons

```Java
    public static HMesh createHMesh(List<int[]> faces) {
         return new FaceSource(faces).toHMesh().orElseThrow(IllegalStateException::new);
     }
```

#### 2. A function creating a [HMesh2D](../src/hgeom/hmesh/elements/HMesh2D.java) from a list of polygons and an array of 2D vertex coordinates

```Java
     public static HMesh2D createHMesh2D(int[][] faces, double[][] vertexCoords) {

          // Create face source
          FaceSource faceSource = new FaceSource(faces);

          // Create vertex coordinates source
          Coord2DSource coord2DSource = new Coord2DSource(vertexCoords);

          // Build the half-edge data structure from the faces and the coordinates
          return faceSource.toHMesh(coord2DSource).orElseThrow(IllegalStateException::new);
     }
```

#### 3. Assign values to polygons according to values attached to their vertices

```Java
   // The polygons as arrays of indices to vertices: 
   int[][] faces = ...;

   // The values attached to the vertices
   double[] vertexValues = ...
   
   // Create a faces source
   FaceSource faceSource = new FaceSource(faces);

   // Create a converter to HMesh
   ToHMeshConverter converter = new ToHMeshConverter();

   // Build the half-edge data structure from the faces
   HConversion<HMesh> conversion = converter.convert(faceSource)
      .orElseThrow(RuntimeException::new);

   // Get the created half-edge data structure
   HMesh mesh = conversion.mesh();

   // Map the original vertex weights to the half-edge data structure's vertices
   HDData<HVertex> meshVertexValues = conversion.meshVertexDoubleData(i -> vertexValues[i]);

   // Create a data collection for the half-edge data structure's faces
   HDData<HFace> meshFaceValues = mesh.createFaceDoubleData();

   // Set each face value as the sum of the values associated to the vertices on its border
   meshFaceValues.setAll(f -> {
       double sum = 0;

       // Compute the sum of the value assigned to the vertices of the face border
       for (HVertex v : f.vertices()) {
          sum += meshVertexValues.get(v);
       }

       // Assign the computed value to the face
       return sum;
   });
```

#### 4. Assign values to vertices according to values attached to surrounding polygons

```Java
   // The polygons as arrays of indices to vertices: 
   int[][] faces = ...;

   // The values attached to the polygons
   double[] faceValues = ...
   
   // Create a faces source
   FaceSource faceSource = new FaceSource(faces);

   // Create a converter to HMesh
   ToHMeshConverter converter = new ToHMeshConverter();

   // Build the half-edge data structure from the faces
   HConversion<HMesh> conversion = converter.convert(faceSource)
      .orElseThrow(RuntimeException::new);

   // Get the created half-edge data structure
   HMesh mesh = conversion.mesh();

   // Map the original face values to the half-edge data structure's faces
   HDData<HFace> meshFaceValues = conversion.meshFaceDoubleData(i -> faceValues[i]);

   // Create a data collection for the half-edge data structure's vertices
   HDData<HVertex> meshVertexValues = mesh.createVertexDoubleData();

   // Set each vertex value as the average of the values associated to its neighboring polygons
   meshVertexValues.setAll(vertex -> {
        double mean = 0;
        int count = 0;

        // Iterate on all the polygons connected to the vertex
        for (HFace face : vertex.outgoingEdges().map(HEdge::face)) {
             mean += meshFaceValues.get(face);
             count += 1;
        }

        // A vertex always connected to several faces, so count > 0
        return mean / count;
   });
```

#### 5. Remove from a polygonal mesh all vertices whose associated value is superior to a limit 

```Java
   // The polygons as arrays of indices to vertices: 
   int[][] faces = ...;

   // The values attached to the vertices
   int[] vertexValues = ...

   // The limit
   int limit = ...
   
   // Create a faces source
   FaceSource faceSource = new FaceSource(faces);

   // Create a converter to HMesh
   ToHMeshConverter converter = new ToHMeshConverter();

   // Build the half-edge data structure from the facessources
   HConversion<HMesh> conversion = converter.convert(faceSource)
      .orElseThrow(RuntimeException::new);

   // Get the created half-edge data structure
   HMesh mesh = conversion.mesh();

   // Map the original vertex weights to the half-edge data structure's vertices
   HIData<HVertex> meshVertexValues = conversion.meshVertexIntData(i -> vertexValues[i]);

   // Get the vertices associated with a value > limit
   List<HVertex> verticesToRemove = mesh.vertices()
         .filter(v -> meshVertexValues.get(v) > limit)
         .collect(Collectors.toList());

   for (HVertex vertex : verticesToRemove) {
      mesh.removeVertex(vertex);
   }
```
