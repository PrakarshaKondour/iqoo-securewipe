from setuptools import setup, find_packages

setup(
    name="wipeproof-officekit",
    version="1.0.0",
    packages=find_packages(),
    install_requires=[
        "flask>=3.0.0"
    ],
    entry_points={
        "console_scripts": [
            "wipeproof-server=server:app.run"
        ]
    }
)
