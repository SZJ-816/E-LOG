<template>
  <div class="three-bg" ref="container">
    <canvas ref="canvas"></canvas>
    <div class="cursor-glow" ref="cursorGlow"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as THREE from 'three'

const container = ref(null)
const canvas = ref(null)
const cursorGlow = ref(null)

let scene, camera, renderer, particles, gridLines
let animationId
let mouseX = 0, mouseY = 0
let targetMouseX = 0, targetMouseY = 0
let cursorX = 0, cursorY = 0

onMounted(() => {
  initThree()
  animate()
  window.addEventListener('resize', onResize)
  window.addEventListener('mousemove', onMouseMove)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  window.removeEventListener('mousemove', onMouseMove)
  cancelAnimationFrame(animationId)
  if (renderer) {
    renderer.dispose()
    renderer.forceContextLoss()
  }
})

function onMouseMove(event) {
  targetMouseX = (event.clientX / window.innerWidth - 0.5) * 2
  targetMouseY = (event.clientY / window.innerHeight - 0.5) * 2
  cursorX = event.clientX
  cursorY = event.clientY
  if (cursorGlow.value) {
    cursorGlow.value.style.transform = `translate(${cursorX}px, ${cursorY}px)`
  }
}

function initThree() {
  scene = new THREE.Scene()

  camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 1, 5000)
  camera.position.z = 600

  renderer = new THREE.WebGLRenderer({
    canvas: canvas.value,
    alpha: true,
    antialias: false,
    powerPreference: 'high-performance'
  })
  renderer.setSize(window.innerWidth, window.innerHeight)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 1.5))
  renderer.setClearColor(0x0a0a0a, 1)

  createParticleField()
  createGrid()
}

function createParticleField() {
  const count = 3000
  const geometry = new THREE.BufferGeometry()
  const positions = new Float32Array(count * 3)
  const colors = new Float32Array(count * 3)
  const sizes = new Float32Array(count)
  const speeds = new Float32Array(count)

  const palette = [
    new THREE.Color('#6366f1'),
    new THREE.Color('#a855f7'),
    new THREE.Color('#22d3ee'),
    new THREE.Color('#818cf8'),
    new THREE.Color('#c084fc'),
  ]

  for (let i = 0; i < count; i++) {
    const i3 = i * 3
    positions[i3] = (Math.random() - 0.5) * 3000
    positions[i3 + 1] = (Math.random() - 0.5) * 2000
    positions[i3 + 2] = (Math.random() - 0.5) * 2000 - 200

    const color = palette[Math.floor(Math.random() * palette.length)]
    const brightness = 0.3 + Math.random() * 0.7
    colors[i3] = color.r * brightness
    colors[i3 + 1] = color.g * brightness
    colors[i3 + 2] = color.b * brightness

    sizes[i] = Math.random() * 3 + 0.5
    speeds[i] = 0.2 + Math.random() * 0.8
  }

  geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))
  geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3))
  geometry.setAttribute('size', new THREE.BufferAttribute(sizes, 1))
  geometry.setAttribute('speed', new THREE.BufferAttribute(speeds, 1))

  const material = new THREE.ShaderMaterial({
    uniforms: {
      uTime: { value: 0 },
      uMouse: { value: new THREE.Vector2(0, 0) },
      uPixelRatio: { value: Math.min(window.devicePixelRatio, 1.5) }
    },
    vertexShader: `
      attribute float size;
      attribute float speed;
      varying vec3 vColor;
      varying float vAlpha;
      uniform float uTime;
      uniform vec2 uMouse;
      uniform float uPixelRatio;

      void main() {
        vColor = color;
        vec3 pos = position;
        pos.x += sin(uTime * speed * 0.3 + position.y * 0.01) * 20.0;
        pos.y += cos(uTime * speed * 0.2 + position.x * 0.01) * 15.0;

        float mouseDist = length(pos.xy - uMouse * 500.0);
        float mouseInfluence = smoothstep(400.0, 0.0, mouseDist);
        pos.xy += normalize(pos.xy - uMouse * 500.0) * mouseInfluence * 60.0;

        vAlpha = 0.4 + mouseInfluence * 0.6;

        vec4 mvPosition = modelViewMatrix * vec4(pos, 1.0);
        gl_PointSize = size * uPixelRatio * (200.0 / -mvPosition.z) * (1.0 + mouseInfluence * 2.0);
        gl_Position = projectionMatrix * mvPosition;
      }
    `,
    fragmentShader: `
      varying vec3 vColor;
      varying float vAlpha;

      void main() {
        float dist = length(gl_PointCoord - vec2(0.5));
        if (dist > 0.5) discard;
        float alpha = smoothstep(0.5, 0.05, dist) * vAlpha;
        vec3 glow = vColor * (1.0 + smoothstep(0.2, 0.0, dist) * 0.8);
        gl_FragColor = vec4(glow, alpha);
      }
    `,
    transparent: true,
    vertexColors: true,
    blending: THREE.AdditiveBlending,
    depthWrite: false
  })

  particles = new THREE.Points(geometry, material)
  scene.add(particles)
}

function createGrid() {
  const gridSize = 40
  const divisions = 30
  const step = gridSize / divisions
  const vertices = []

  for (let i = -divisions / 2; i <= divisions / 2; i++) {
    vertices.push(-gridSize / 2, 0, i * step, gridSize / 2, 0, i * step)
    vertices.push(i * step, 0, -gridSize / 2, i * step, 0, gridSize / 2)
  }

  const geometry = new THREE.BufferGeometry()
  geometry.setAttribute('position', new THREE.Float32BufferAttribute(vertices, 3))

  const material = new THREE.LineBasicMaterial({
    color: 0x6366f1,
    transparent: true,
    opacity: 0.03,
    blending: THREE.AdditiveBlending
  })

  gridLines = new THREE.LineSegments(geometry, material)
  gridLines.position.y = -300
  gridLines.scale.set(50, 1, 50)
  scene.add(gridLines)
}

let lastTime = performance.now()

function animate() {
  animationId = requestAnimationFrame(animate)

  const now = performance.now()
  const deltaTime = Math.min((now - lastTime) / 1000, 0.05)
  lastTime = now
  const time = now * 0.001

  mouseX += (targetMouseX - mouseX) * 0.03
  mouseY += (targetMouseY - mouseY) * 0.03

  if (particles) {
    particles.rotation.y += 0.00015
    particles.material.uniforms.uTime.value = time
    particles.material.uniforms.uMouse.value.set(mouseX, -mouseY)
  }

  if (gridLines) {
    gridLines.rotation.y += 0.0003
    gridLines.material.opacity = 0.02 + Math.sin(time * 0.5) * 0.01
  }

  camera.position.x = mouseX * 60
  camera.position.y = -mouseY * 40
  camera.lookAt(0, 0, -200)

  renderer.render(scene, camera)
}

function onResize() {
  camera.aspect = window.innerWidth / window.innerHeight
  camera.updateProjectionMatrix()
  renderer.setSize(window.innerWidth, window.innerHeight)
  if (particles) particles.material.uniforms.uPixelRatio.value = Math.min(window.devicePixelRatio, 1.5)
}
</script>

<style scoped>
.three-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background: #0a0a0a;
}

.three-bg canvas {
  display: block;
}

.cursor-glow {
  position: fixed;
  top: 0;
  left: 0;
  width: 400px;
  height: 400px;
  margin-left: -200px;
  margin-top: -200px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.08) 0%, transparent 70%);
  pointer-events: none;
  z-index: 1;
  transition: transform 0.1s ease-out;
  will-change: transform;
}
</style>
