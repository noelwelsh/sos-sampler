

# sos-sampler
UI to test and showcase the Sounds of Scala's sampler instrument 

<img width="1512" alt="Screenshot 2024-08-25 at 17 20 51" src="https://github.com/user-attachments/assets/ddba2eb0-0fa5-455d-a4a8-ffcc0344c770">

To develop:

- `sbt fastLinkJS` to compile the Scala code
- `npm run preview` to build and serve locally
- `cp -r resources dist/resources` to move the samples into a location the local server can find


To publish:

The commands in `.github/workflows/ci.yml` will automatically build and publish a site containing the application. To serve this from Github pages:

1. Settings > Pages > Deploy from a branch
2. Branch is `gh-pages`

Note that your main branch must be called `main` (the current default), not `master` (which older repositories might use.) See [renaming a branch](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-branches-in-your-repository/renaming-a-branch)

Then visit <username>.github.io/sos-sampler to see the deployed site.
