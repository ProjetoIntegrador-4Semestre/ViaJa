import argparse
import difflib
from pathlib import Path
import subprocess


def main():
    parser = argparse.ArgumentParser(description="Gera o cumulativo de fontes e confere aplicação sem alterar a API original.")
    parser.add_argument("--original", type=Path, required=True)
    parser.add_argument("--updated", type=Path, required=True)
    args = parser.parse_args()
    original, updated = args.original.resolve(), args.updated.resolve()
    if original == updated:
        raise SystemExit("Use uma cópia isolada diferente da fonte original.")
    paths = [Path("pom.xml"), Path("src/main/resources/application-persistent.properties")]
    paths += [path.relative_to(updated) for directory in ["src/main/java", "src/test/java"] for path in sorted((updated / directory).rglob("*.java"))]
    output = []
    for path in paths:
        before, after = original / path, updated / path
        left = before.read_text().splitlines(keepends=True) if before.exists() else []
        right = after.read_text().splitlines(keepends=True)
        if left == right:
            continue
        output.append(f"diff --git a/{path} b/{path}\n")
        if not before.exists():
            output.append("new file mode 100644\n")
        output.extend(difflib.unified_diff(left, right, fromfile=f"a/{path}" if before.exists() else "/dev/null", tofile=f"b/{path}"))
    patch = Path(__file__).resolve().with_name("all-packages.patch")
    candidate = patch.with_suffix(".candidate.patch")
    candidate.write_text("".join(output))
    try:
        subprocess.run(["git", "-C", str(original), "apply", "--check", str(candidate)], check=True)
        candidate.replace(patch)
    finally:
        candidate.unlink(missing_ok=True)
    print("Patch cumulativo atualizado; aplicação conferida sem escrita na API original.")


if __name__ == "__main__":
    main()
