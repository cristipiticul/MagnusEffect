#version 330 core
 
// Input vertex data, different for all executions of this shader.
layout(location = 0) in vec2 vertexPosition_modelspace;
layout(location = 1) in vec2 vertexUV;
 
// Output data ; will be interpolated for each fragment.
out vec2 UV;

uniform mat3 MVP;
 
void main(){
    vec3 pos = MVP * vec3(vertexPosition_modelspace, 1.0); // x, y, w
 
    // Output position of the vertex, in clip space : MVP * position
    gl_Position.x = pos.x;
    gl_Position.y = pos.y;
    gl_Position.z = 0.0;
    gl_Position.w = pos.z;
 
    // UV of the vertex. No special space for this one.
    UV = vertexUV;
}