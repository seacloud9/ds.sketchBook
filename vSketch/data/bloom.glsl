#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

#define PROCESSING_TEXTURE_SHADER

const float blurSize = 1.0/16.0;
const float intensity = 0.10;
uniform sampler2D texture;
uniform vec2 texOffset;

varying vec4 vertColor;
varying vec4 vertTexCoord;
void main()
{
   vec4 sum = vec4(0);

   //thank you! http://www.gamerendering.com/2008/10/11/gaussian-blur-filter-shader/ for the 
   //blur tutorial
   // blur in y (vertical)
   // take nine samples, with the distance blurSize between them
   sum += texture2D(texture, (vertTexCoord.xy + vec2(texOffset.x - 4.0*blurSize, texOffset.y))) * 0.05;
   sum += texture2D(texture, (vertTexCoord.xy + vec2(texOffset.x - 3.0*blurSize, texOffset.y))) * 0.09;
   sum += texture2D(texture, (vertTexCoord.xy + vec2(texOffset.x - 2.0*blurSize, texOffset.y))) * 0.12;
   sum += texture2D(texture, (vertTexCoord.xy + vec2(texOffset.x - blurSize, texOffset.y))) * 0.15;
   sum += texture2D(texture, (vertTexCoord.xy + vec2(texOffset.x, texOffset.y))) * 0.16;
   sum += texture2D(texture, (vertTexCoord.xy + vec2(texOffset.x + blurSize, texOffset.y))) * 0.15;
   sum += texture2D(texture, (vertTexCoord.xy + vec2(texOffset.x + 2.0*blurSize, texOffset.y))) * 0.12;
   sum += texture2D(texture, (vertTexCoord.xy + vec2(texOffset.x + 3.0*blurSize, texOffset.y))) * 0.09;
   sum += texture2D(texture, (vertTexCoord.xy + vec2(texOffset.x + 4.0*blurSize, texOffset.y))) * 0.05;
  
  // blur in y (vertical)
   // take nine samples, with the distance blurSize between them
   sum += texture2D(texture, (vertTexCoord.xy + vec2(texOffset.x, texOffset.y - 4.0*blurSize))) * 0.05;
   sum += texture2D(texture, (vertTexCoord.xy + vec2(texOffset.x, texOffset.y - 3.0*blurSize))) * 0.09;
   sum += texture2D(texture, (vertTexCoord.xy + vec2(texOffset.x, texOffset.y - 2.0*blurSize))) * 0.12;
   sum += texture2D(texture, (vertTexCoord.xy + vec2(texOffset.x, texOffset.y - blurSize))) * 0.15;
   sum += texture2D(texture, (vertTexCoord.xy + vec2(texOffset.x, texOffset.y))) * 0.16;
   sum += texture2D(texture, (vertTexCoord.xy + vec2(texOffset.x, texOffset.y + blurSize))) * 0.15;
   sum += texture2D(texture, (vertTexCoord.xy + vec2(texOffset.x, texOffset.y + 2.0*blurSize))) * 0.12;
   sum += texture2D(texture, (vertTexCoord.xy + vec2(texOffset.x, texOffset.y + 3.0*blurSize))) * 0.09;
   sum += texture2D(texture, (vertTexCoord.xy + vec2(texOffset.x, texOffset.y + 4.0*blurSize))) * 0.05;

   //increase blur with intensity!
       gl_FragColor = sum * sin(0.25)+ texture2D(texture, texOffset);

}