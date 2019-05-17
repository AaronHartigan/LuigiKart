var JavaPackages = new JavaImporter(
	Packages.ray.rage.scene.SceneManager,
	Packages.ray.rage.scene.SceneNode,
	Packages.ray.rml.Vector3f,
	Packages.ray.rml.Matrix3f,
	Packages.ray.rml.Degreef
);

// creates a RAGE object - in this case a light
with (JavaPackages)
{
	if (sm) {
		var locations = [
			{ x: -65.37, y: -3, z: -40.38, pitch: -90, roll: -45, scale: 1 },
			{ x: -60.37, y: -3, z: -27.38, pitch: -90, roll: -45, scale: 1 },
			{ x: -68.37, y: -3, z: 10.38, pitch: -90, roll: -45, scale: 1 },
			{ x: -65.37, y: -3, z: 20.38, pitch: -90, roll: -45, scale: 1 },
			{ x: -40.37, y: -3, z: 10.38, pitch: -90, roll: -45, scale: 1 },
			{ x: 45.37, y: -3, z: -67.38, pitch: -90, roll: -45, scale: 1 },
			{ x: 50.37, y: -3, z: -67.38, pitch: -90, roll: -45, scale: 1 },
			{ x: 68.37, y: -3, z: -67.38, pitch: -90, roll: -45, scale: 1 },
			{ x: 10.37, y: -6, z: -67.38, pitch: -90, roll: 45, scale: 2 },
			{ x: 5.37, y: -6, z: -47.38, pitch: -90, roll: 45, scale: 2 },
		];
		for (var i = 0; i < locations.length; i++) {
			var tree = sm.getSceneNode("tree" + i);
			tree.setLocalPosition(Vector3f.createFrom(locations[i].x, locations[i].y, locations[i].z));
			tree.setLocalRotation(Matrix3f.createIdentityMatrix());
			tree.pitch(Degreef.createFrom(locations[i].pitch));
			tree.roll(Degreef.createFrom(locations[i].roll));
			tree.setLocalScale(locations[i].scale, locations[i].scale, locations[i].scale);
		}
	}
}

print('Script has executed');